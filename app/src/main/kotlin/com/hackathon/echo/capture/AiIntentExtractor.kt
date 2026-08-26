package com.hackathon.echo.capture

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * The AI intent layer. Sends captured content to Google's Gemini API and asks it to
 * return a structured classification (title / category / intent) as JSON.
 *
 * Design contract:
 *  - This is the PRIMARY classifier when a GEMINI_API_KEY is configured. It never
 *    replaces the deterministic [com.hackathon.echo.ocr.Categorizer] /
 *    [com.hackathon.echo.ocr.EntityExtractor] — those remain the fallback. If the key
 *    is blank, or the call times out / fails / returns an invalid category, [extract]
 *    returns null and the caller falls back to the rules engine.
 *  - Bounded, non-blocking (Dispatchers.IO + withTimeoutOrNull). Never throws.
 *  - The API key is read from BuildConfig and is NEVER logged, and the prompt/response
 *    bodies are never logged (only lengths / error class / HTTP code).
 *  - No scraping, no unofficial endpoints — this is the official Generative Language API.
 */
object AiIntentExtractor {

    /** Categories Echo understands. The model is constrained to exactly these. */
    private val ALLOWED_CATEGORIES = setOf(
        "EVENT", "RECIPE", "TOOL", "PLACE", "TOPIC", "TASK", "NOTE"
    )

    /** Suggested actions Echo can act on. Anything else is dropped to null. */
    private val ALLOWED_ACTIONS = setOf(
        "OPEN_MAPS", "SEARCH", "SET_REMINDER", "COPY"
    )

    /**
     * Latest flash model on the v1beta generateContent endpoint:
     *   https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent
     *
     * Auth is the x-goog-api-key header (equivalent to ?key=... in the URL); the key is read
     * from BuildConfig and NEVER hardcoded here. If this returns HTTP 404 ("model not found
     * for API version v1beta"), the ID has been retired — verify the current list before
     * changing it:  curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_KEY"
     * A 404 is a model/endpoint problem, NOT auth (auth failures surface as 401/403).
     */
    private const val MODEL = "gemini-3.7-flash"

    /**
     * LLM generation needs more headroom than a metadata GET; still hard-bounded. YouTube
     * shares arrive as enriched oEmbed text (Title / Description / Context) baked into the
     * prompt by the caller — a pure text payload, never a blocking video-URI resolution — so
     * a 10s budget is plenty before we fall back to the rules engine. The socket read/connect
     * timeouts below sit UNDER this so a stalled call abandons cleanly and returns null (→
     * fallback) instead of being cancelled mid-flight.
     */
    private const val TIMEOUT_MS = 10000L

    data class AiIntent(
        val title: String,
        val category: String,
        val intent: String,
        val summary: String?,
        val action: String?,
        /** Raw date string from the model ("YYYY-MM-DD" or a relative phrase), or null. */
        val reminderDate: String?
    )

    /**
     * Classifies [content] via Gemini. Returns null (→ deterministic fallback) on blank
     * key, timeout, network/HTTP error, or an unparseable / out-of-vocabulary response.
     */
    suspend fun extract(content: String, apiKey: String): AiIntent? {
        // Trim defensively: local.properties values can carry a trailing newline / carriage
        // return, and any stray char here would corrupt the request and make Google reject it.
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            Log.d("EchoCapture", "AI: no Gemini key configured - using deterministic fallback")
            Log.e("ECHO_DEBUG", "Gemini failed, switching to local rules fallback (no API key configured)")
            return null
        }
        if (content.isBlank()) return null

        // Diagnostic: is a Gemini key actually reaching this code? Google issues both the
        // legacy "AIza..." keys and the newer "AQ...." Authentication Keys; both are valid on
        // the native generateContent endpoint. We only report presence + length, NEVER the key.
        Log.d("ECHO_DEBUG", "API Key present: ${cleanKey.isNotEmpty()} (length=${cleanKey.length})")

        Log.d("ECHO_DEBUG", "Sending prompt to Gemini API... (contentLen=${content.length})")
        // Single text path. YouTube links are handled as ENRICHED TEXT — the caller bakes the
        // oEmbed metadata (Title / Description / Context) plus the URL into `content`. We do NOT
        // send a fileData.fileUri video part: the Generative Language API rejects raw YouTube
        // URLs in fileData with HTTP 404, so the enriched text prompt is the primary payload.
        return withTimeoutOrNull(TIMEOUT_MS) {
            postGenerateContent(buildRequestBody(content), cleanKey)
        } ?: run {
            Log.e("ECHO_DEBUG", "Gemini failed, switching to local rules fallback (timed out after ${TIMEOUT_MS}ms)")
            null
        }
    }

    /**
     * The shared classification instructions (schema + category guidance). Both the text and
     * the video requests reuse this, so the model always returns the same JSON shape that
     * [parseResponse] expects.
     */
    private fun classificationInstructions(today: String): String = """
        You classify and summarise a piece of content a user saved to a personal memory app.
        Today's date is $today. Respond with ONLY a JSON object with EXACTLY these keys:
        {
          "category": one of EVENT, RECIPE, TOOL, PLACE, TOPIC, TASK, NOTE,
          "title": short human-readable title (max 60 chars, no URLs),
          "summary": 2-3 concise bullet points summarising the content (use "\n- " between bullets), or null,
          "intent": short actionable label (max 40 chars, e.g. "Cook this recipe", "Attend event"),
          "action": one of OPEN_MAPS, SEARCH, SET_REMINDER, COPY,
          "reminder_date": an absolute date "YYYY-MM-DD" if the content mentions or implies a date/event
                           (resolve relative phrases like "tomorrow" or "this weekend" against today's date),
                           otherwise null
        }
        Category guidance: somewhere to go / travel / "places to visit" (even if a city name like
        Pune, Mumbai or Bangalore is mentioned) => PLACE, not EVENT. A dated happening => EVENT.
        Cooking => RECIPE. An app/product/utility => TOOL. A subject to learn => TOPIC.
        Something to do => TASK. Anything else => NOTE.
    """.trimIndent()

    /** Builds the generateContent request for plain text content, forcing a strict JSON response. */
    private fun buildRequestBody(content: String): String {
        val today = java.time.LocalDate.now().toString() // YYYY-MM-DD, device local date
        val prompt = classificationInstructions(today) + "\n\nCONTENT:\n" + content
        Log.d("ECHO_DEBUG", "Final prompt sent to Gemini:\n$prompt")
        val part = JSONObject().put("text", prompt)
        val contentObj = JSONObject().put("parts", JSONArray().put(part))
        return wrapContents(contentObj)
    }

    /** Wraps a single content object with the shared generationConfig (strict JSON, low temp). */
    private fun wrapContents(contentObj: JSONObject): String {
        // thinkingBudget = 0 disables the model's "thinking" pass. This is a simple JSON
        // extraction, so extended reasoning only adds latency (and tokens) with no benefit —
        // pure text prompts on 3.7 Flash then return in ~1-2s, well inside the 10s budget.
        val thinkingConfig = JSONObject().put("thinkingBudget", 0)
        val genConfig = JSONObject()
            .put("responseMimeType", "application/json")
            .put("temperature", 0.1)
            .put("thinkingConfig", thinkingConfig)
        return JSONObject()
            .put("contents", JSONArray().put(contentObj))
            .put("generationConfig", genConfig)
            .toString()
    }

    /**
     * Performs a single generateContent POST with [requestBody] and parses the result.
     * Runs on Dispatchers.IO; never throws; returns null on HTTP error / network failure /
     * unparseable body. The key goes in the x-goog-api-key header and is never logged.
     */
    private suspend fun postGenerateContent(requestBody: String, cleanKey: String): AiIntent? =
        withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                // Key goes in the x-goog-api-key header (not the query string) so a stray
                // character can never corrupt the URL. Works for both AIza and AQ. keys.
                // Endpoint MUST be v1beta — generateContent is not served on v1.
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" +
                    "$MODEL:generateContent"
                // Resolved request line, key REDACTED (auth is the x-goog-api-key header, so the
                // key never appears in the URL). A 404 on this line almost always means a
                // bad/retired MODEL — not an auth problem (that would surface as 401/403).
                Log.d("ECHO_DEBUG", "Gemini POST -> $endpoint  [x-goog-api-key: ***redacted*** len=${cleanKey.length}]")
                conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 3000
                    // Socket timeouts sum to under the 10s coroutine budget (3s + 6.5s = 9.5s)
                    // so a stalled read is abandoned by the socket itself — returning a clean
                    // null for the fallback — before withTimeoutOrNull cancels the coroutine.
                    readTimeout = 6500
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-goog-api-key", cleanKey)
                }
                conn.outputStream.use { it.write(requestBody.toByteArray()) }

                val code = conn.responseCode
                if (code != HttpURLConnection.HTTP_OK) {
                    // Never log the key/URL/body.
                    Log.w("EchoCapture", "AI: Gemini request returned HTTP $code")
                    Log.e("ECHO_DEBUG", "Gemini failed, switching to local rules fallback (HTTP $code)")
                    return@withContext null
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                parseResponse(body)
            } catch (e: Exception) {
                Log.w("EchoCapture", "AI: Gemini call failed (${e.javaClass.simpleName})")
                Log.e("ECHO_DEBUG", "Gemini failed, switching to local rules fallback: ${e.message}")
                null
            } finally {
                conn?.disconnect()
            }
        }

    /** Extracts the model's JSON text and validates it into an [AiIntent], or null. */
    private fun parseResponse(body: String): AiIntent? {
        val candidates = JSONObject(body).optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val parts = candidates.getJSONObject(0)
            .optJSONObject("content")?.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null
        val text = parts.getJSONObject(0).optString("text").trim()
        if (text.isBlank()) return null
        Log.d("ECHO_DEBUG", "Gemini Result: $text")

        val json = try { JSONObject(text) } catch (e: Exception) { return null }
        val title = json.optString("title").trim()
        val category = json.optString("category").trim().uppercase()
        val intent = json.optString("intent").trim()
        if (title.isBlank() || category !in ALLOWED_CATEGORIES) {
            Log.d("EchoCapture", "AI: response missing title or invalid category - fallback")
            return null
        }
        val summary = json.optString("summary").trim()
            .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        val action = json.optString("action").trim().uppercase()
            .takeIf { it in ALLOWED_ACTIONS }
        val reminderDate = json.optString("reminder_date").trim()
            .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        val cappedTitle = if (title.length > 60) title.take(57) + "..." else title
        return AiIntent(
            title = cappedTitle,
            category = category,
            intent = intent.ifBlank { "Remember this" },
            summary = summary,
            action = action,
            reminderDate = reminderDate
        )
    }
}
