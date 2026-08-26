"""
Echo backend — YouTube Short summarization & intent extraction.

A single endpoint, POST /api/analyze-youtube, that the Echo Android app calls with a
YouTube URL. It fetches the transcript (falling back to oEmbed metadata), asks Gemini to
classify + summarize it, and returns structured JSON the app can persist directly.

Run:
    pip install -r requirements.txt
    # PowerShell:  $env:GEMINI_API_KEY="your_key"
    # bash:        export GEMINI_API_KEY=your_key
    uvicorn main:app --reload --port 8000

SECURITY: this endpoint is UNAUTHENTICATED by default. Set ECHO_BACKEND_API_KEY to require
an X-API-Key header (see require_api_key). Never commit real keys — keep them in the env/.env.
"""
from __future__ import annotations

import os
import re
import asyncio
import datetime as dt
from pathlib import Path
from typing import Optional, Literal

import httpx
from fastapi import FastAPI, HTTPException, Header
from pydantic import BaseModel, Field

from dotenv import load_dotenv
from google import genai
from google.genai import types

# Load backend/.env (gitignored) so keys can live in a file instead of the shell env.
# Real environment variables always win — load_dotenv does not override them by default.
load_dotenv(Path(__file__).with_name(".env"))

MODEL = os.environ.get("GEMINI_MODEL", "gemini-2.5-flash")
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "").strip()
BACKEND_API_KEY = os.environ.get("ECHO_BACKEND_API_KEY", "").strip()

# Fail fast if the model key is missing — the whole service depends on it.
if not GEMINI_API_KEY:
    raise RuntimeError("GEMINI_API_KEY is not set. Export it before starting the server.")

client = genai.Client(api_key=GEMINI_API_KEY)
app = FastAPI(title="Echo YouTube Analyzer", version="1.0.0")


# ---- request / response models -------------------------------------------------

class AnalyzeRequest(BaseModel):
    url: str = Field(..., description="A YouTube Shorts / watch / youtu.be URL")


class VideoAnalysis(BaseModel):
    category: Literal["EVENT", "RECIPE", "TOOL", "PLACE", "TOPIC"]
    title: str
    summary: str            # 2-3 bullet points, newline-separated
    intent: str
    reminder_date: Optional[str] = None   # "YYYY-MM-DD" or null


# ---- helpers -------------------------------------------------------------------

_YT_ID = re.compile(
    r"(?:youtube\.com/shorts/|youtu\.be/|youtube\.com/embed/|[?&]v=)([A-Za-z0-9_-]{6,})",
    re.IGNORECASE,
)


def extract_video_id(url: str) -> Optional[str]:
    m = _YT_ID.search(url)
    return m.group(1) if m else None


async def fetch_transcript(video_id: str) -> Optional[str]:
    """Auto-generated/manual captions via youtube-transcript-api (blocking → thread)."""
    def _blocking() -> Optional[str]:
        try:
            # youtube-transcript-api >= 1.0 instance API.
            from youtube_transcript_api import YouTubeTranscriptApi
            fetched = YouTubeTranscriptApi().fetch(video_id)
            text = " ".join(snippet.text for snippet in fetched).strip()
            return text or None
        except Exception:
            # No captions, disabled transcripts, region block, etc. — caller falls back.
            return None
    return await asyncio.to_thread(_blocking)


async def fetch_oembed(url: str) -> Optional[str]:
    """Key-free oEmbed fallback: returns 'Title — by Author' or None."""
    try:
        async with httpx.AsyncClient(timeout=5.0) as http:
            r = await http.get(
                "https://www.youtube.com/oembed",
                params={"url": url, "format": "json"},
            )
        if r.status_code != 200:
            return None
        data = r.json()
        title = (data.get("title") or "").strip()
        author = (data.get("author_name") or "").strip()
        if not title:
            return None
        return f"{title} — by {author}" if author else title
    except Exception:
        return None


SYSTEM_PROMPT = (
    "You classify and summarise a YouTube video a user saved to a personal memory app. "
    "Respond with ONLY JSON matching the schema. category must be exactly one of "
    "EVENT, RECIPE, TOOL, PLACE, TOPIC. title: short, no URLs. summary: 2-3 concise bullet "
    "points (prefix each with '- ', newline-separated). intent: a short actionable label. "
    "reminder_date: an absolute 'YYYY-MM-DD' if the content implies a date/event (resolve "
    "relative phrases against today's date), otherwise null."
)


def analyze_with_gemini(source_text: str) -> VideoAnalysis:
    """Blocking Gemini call — run via asyncio.to_thread from the async endpoint."""
    today = dt.date.today().isoformat()
    prompt = f"Today's date is {today}.\n\nVIDEO CONTENT:\n{source_text}"
    resp = client.models.generate_content(
        model=MODEL,
        contents=prompt,
        config=types.GenerateContentConfig(
            system_instruction=SYSTEM_PROMPT,
            response_mime_type="application/json",
            response_schema=VideoAnalysis,
            temperature=0.1,
        ),
    )
    # The SDK hydrates .parsed from the schema; fall back to parsing raw text if not.
    parsed = getattr(resp, "parsed", None)
    if isinstance(parsed, VideoAnalysis):
        return parsed
    return VideoAnalysis.model_validate_json(resp.text)


def require_api_key(x_api_key: Optional[str]) -> None:
    """No-op unless ECHO_BACKEND_API_KEY is set; then an exact X-API-Key match is required."""
    if BACKEND_API_KEY and x_api_key != BACKEND_API_KEY:
        raise HTTPException(status_code=401, detail="Invalid or missing X-API-Key")


# ---- endpoints -----------------------------------------------------------------

@app.get("/health")
def health() -> dict:
    return {"status": "ok", "model": MODEL}


@app.post("/api/analyze-youtube", response_model=VideoAnalysis)
async def analyze_youtube(
    req: AnalyzeRequest,
    x_api_key: Optional[str] = Header(default=None),
) -> VideoAnalysis:
    require_api_key(x_api_key)

    video_id = extract_video_id(req.url)
    if not video_id:
        raise HTTPException(status_code=400, detail="Not a recognisable YouTube URL")

    # 1) transcript (preferred), 2) oEmbed metadata fallback.
    source = await fetch_transcript(video_id)
    if not source:
        source = await fetch_oembed(req.url)
    if not source:
        raise HTTPException(
            status_code=422,
            detail="Could not fetch transcript or metadata for this video",
        )

    try:
        return await asyncio.to_thread(analyze_with_gemini, source)
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Gemini analysis failed: {type(e).__name__}")
