## Project: Echo — iQOO Hackathon 2026

**Track:** Open Innovation (also fits Productivity)
**City:** Pune, submission deadline 1 Sep

### The Problem
People constantly save Instagram Reels and YouTube Shorts — recipes, event/hackathon posts, tool recommendations, places to visit — with the intention of returning to them. In practice, these saves disappear into a graveyard and are never revisited. Existing "video-to-notes" summarizer apps (VidNotes, Memories.ai, NoteGPT, etc.) solve the wrong problem: they make saved content easier to *read*, not easier to *remember*. They're pull-based — you have to go open the app.

### The Idea
Echo is a **forgotten-intentions tracker**, not a summarizer. Users share a Reel/Short (or a screenshot of anything — a tweet, poster, WhatsApp message) directly to Echo via the native Android share sheet. On-device AI extracts the key content and classifies it into a category (recipe / event / tool / place / topic), pulling out key details like dates, names, or locations. The structured item syncs to a companion laptop dashboard via iQOO's Office Kit bridge. Instead of sitting in a static list, Echo proactively resurfaces each item at the moment it's actually relevant — a recipe on the weekend, an event reminder before its date, a tool nudge tied to a calendar event, or a location-based nudge when physically nearby.

### Core Flow
1. **Capture** — Share a reel/short or screenshot to Echo via Android's share sheet.
2. **Extract** — On-device OCR pulls caption/on-screen text (or screenshot text).
3. **Classify** — On-device AI (or keyword-based fallback) categorizes it and extracts key entities (date, name, location).
4. **Sync** — Structured item pushed to a laptop dashboard via Office Kit (screen mirror / clipboard / file transfer).
5. **Resurface** — Item comes back to the user proactively: date-extracted event reminders (primary, most reliable), calendar-tied tool nudges (stretch), location-based nudges when near a saved place (stretch, secondary due to Android geofencing latency), and time-based patterns (e.g. recipes on weekends).

### MVP Scope (build priority order)
1. Share-sheet capture (reels + screenshots) + OCR
2. Keyword-based classification (reliable fallback; on-device LLM via Gemini Nano/ML Kit GenAI as a stretch upgrade)
3. Date extraction → event reminders (most demo-reliable feature)
4. Office Kit sync to laptop dashboard (categorized, not chronological)
5. Stretch goals: on-device LLM classification, calendar-based resurfacing, location-based resurfacing

### Key Differentiators from Competitors
- **Push, not pull** — resurfaces content unprompted, rather than requiring the user to open the app and search
- **On-device processing** — classification runs on-device (not cloud), which is both a privacy pitch and directly scored by the hackathon's hardware-usage rubric
- **Native share-sheet capture** — no copy-pasting links, unlike most competitor tools
- **Multi-input** — accepts both reels/shorts and screenshots through the same pipeline
- **Intent-based categorization** — classifies *why* something was saved, not just *what's in it*

### Hackathon Constraints Shaping the Build
- 30-hour in-person hackathon with an iQOO 15 loaner phone (OriginOS 6)
- **Red Light (~55% of time):** phone + Office Kit only, laptop use restricted — architecture is designed so the "hard" work (capture, OCR, classification) runs entirely on-device, with the laptop as a thin receiver/dashboard
- **Green Light (~45%):** both devices free — used for laptop dashboard build and heavier logic
- Office Kit (the phone-laptop bridge) requires Windows/macOS (team laptops are Windows — compatible)
- $25 in AI credits provided (provider TBD) — reserved for stretch-goal cloud calls, not the core on-device classification step, since that's what's scored

### Scoring Context (why the architecture is shaped this way)
75% jury (End product quality 30%, Novelty & impact 20%, Technical depth 15%, Demo 10%) + 25% device telemetry (Creative on-device phone use 15%, Office Kit bridge usage 10% — tracked automatically, not self-reported)

### Biggest Open Risk
On-device LLM (Gemini Nano/ML Kit GenAI) support/reliability on the loaner iQOO 15 is unconfirmed — plan to test in the first build window, with keyword-based classification as a guaranteed fallback. Location-based resurfacing has known Android geofencing latency (2–6 min), so it's treated as a secondary/stretch feature, not the primary live-demo trigger.