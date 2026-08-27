package com.hackathon.echo.ocr

import android.util.Log
import com.google.gson.Gson
import com.hackathon.echo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cloud AI layer (Gemini). Determines the user's INTENTION and returns strict JSON.
 *
 * Calls the generateContent REST endpoint directly (v1beta) instead of the legacy
 * generativeai SDK: the SDK (0.9.0, discontinued) cannot parse the "thinking" response
 * shape returned by current Flash models (parts carry a thoughtSignature) and throws.
 * The raw REST contract is stable and we control the parsing.
 *
 * Contract: this must NEVER crash or hang the capture flow. A missing key, auth error
 * (401/403), bad model (404), rate-limit, timeout, network failure, malformed JSON, a
 * safety block, or a response that fails validation all return null so the deterministic
 * rules engine takes over.
 */
class AiIntentExtractor {
    private val gson = Gson()
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val modelName = BuildConfig.GEMINI_MODEL

    suspend fun extractIntent(text: String): AiResult? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            Log.w(TAG, "GEMINI_API_KEY not set — using rules fallback.")
            return@withContext null
        }
        if (text.isBlank()) return@withContext null

        val modelJson = callGemini(buildPrompt(text)) ?: return@withContext null

        val raw = try {
            gson.fromJson(modelJson, RawAiResult::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Malformed AI JSON — fallback. head=${modelJson.take(160)}")
            null
        } ?: return@withContext null

        val clean = sanitizeAndValidate(raw)
        if (clean == null) {
            Log.e(TAG, "AI result failed validation — fallback.")
        } else {
            Log.d(TAG, "Gemini OK via '$modelName': ${clean.category}/${clean.intent} action=${clean.action}")
        }
        clean
    }

    /** Performs the REST call and returns the model's raw text (its JSON answer), or null. */
    private fun callGemini(prompt: String): String? {
        var conn: HttpURLConnection? = null
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
            conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 9000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-goog-api-key", apiKey)
            }
            conn.outputStream.use { it.write(buildRequestBody(prompt).toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }?.take(200)
                Log.e(TAG, "Gemini HTTP $code — fallback. ${err.orEmpty()}")
                return null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            return extractModelText(body)
        } catch (e: Exception) {
            // Timeout, network, SSL, provider errors — all fall back safely.
            Log.e(TAG, "Gemini REST failed (${e.javaClass.simpleName}: ${e.message?.take(120)}) — fallback.")
            return null
        } finally {
            conn?.disconnect()
        }
    }

    private fun buildRequestBody(prompt: String): String {
        val parts = JSONArray().put(JSONObject().put("text", prompt))
        val contents = JSONArray().put(JSONObject().put("parts", parts))

        val generationConfig = JSONObject()
            .put("temperature", 0.1)
            .put("maxOutputTokens", 1024)
            .put("responseMimeType", "application/json")

        val safetySettings = JSONArray()
        for (category in SAFETY_CATEGORIES) {
            safetySettings.put(JSONObject().put("category", category).put("threshold", "BLOCK_ONLY_HIGH"))
        }

        return JSONObject()
            .put("contents", contents)
            .put("generationConfig", generationConfig)
            .put("safetySettings", safetySettings)
            .toString()
    }

    /** Pulls the concatenated text out of candidates[0]; ignores thought-only parts. */
    private fun extractModelText(responseBody: String): String? {
        return try {
            val root = JSONObject(responseBody)

            root.optJSONObject("promptFeedback")?.optString("blockReason")
                ?.takeIf { it.isNotBlank() }?.let {
                    Log.w(TAG, "Gemini blocked prompt ($it) — fallback.")
                    return null
                }

            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val parts = candidates.getJSONObject(0)
                .optJSONObject("content")?.optJSONArray("parts") ?: return null

            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val t = parts.getJSONObject(i).optString("text", "")
                if (t.isNotBlank()) sb.append(t)
            }
            sb.toString().trim().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response — fallback. head=${responseBody.take(160)}")
            null
        }
    }

    private fun buildPrompt(text: String): String = """
        You are Echo, a "forgotten-intentions" tracker. A user saved the content below because they INTEND to act on it later. Identify that intention and return STRICT JSON ONLY — no markdown, no code fences, no commentary.

        CATEGORY (choose exactly one): PLACE, EVENT, RECIPE, TOOL, TOPIC, OTHER
        ACTION (choose exactly one): OPEN_MAPS, SET_REMINDER, ADD_CALENDAR, OPEN_URL, SEARCH, NONE
        INTENT: a single UPPERCASE verb for WHY they saved it (VISIT, ATTEND, TRY, COOK, LEARN, READ, WATCH, BUY, EXPLORE, ...).

        Classification rules:
        - Places / cities / cafes / restaurants / forts / travel spots to go to => PLACE, intent VISIT, action OPEN_MAPS.
        - A dated happening (hackathon, concert, conference, meetup, workshop) => EVENT, intent ATTEND, action SET_REMINDER.
        - A city or place NAME by itself is a location, NOT an EVENT.
        - A dish or cooking content => RECIPE, intent COOK, action SEARCH.
        - Apps / software / websites / developer tools => TOOL, intent TRY, action OPEN_URL if a URL is present, else SEARCH.
        - Something to study / read / understand => TOPIC, intent LEARN, action SEARCH.
        - Anything else => OTHER, action NONE.

        Field rules:
        - title: clean human Title Case, max 60 chars, no trailing punctuation.
        - location / date / time / url: include ONLY if explicitly present in the content, otherwise null.
        - summary: one short phrase naming the user's intention (e.g., "Weekend trip idea"), NOT a recap of the content.

        Video rules (content may be labeled VIDEO TITLE / VIDEO DESCRIPTION / VIDEO TRANSCRIPT):
        - A VIDEO TRANSCRIPT is the spoken audio of a short video. Use it ONLY to infer category, intent, and explicitly-spoken entities (dates, deadlines, locations, names). Do NOT transcribe, summarize, or repeat the transcript back in any field.
        - Prefer a concise title describing the video's subject over quoting its words. A spoken date/deadline in the transcript IS a valid date entity even if the title omits it.

        Respond using exactly this JSON shape:
        {"category":"","intent":"","title":"","summary":"","location":null,"date":null,"time":null,"url":null,"action":""}

        Content:
        ${text.take(4000)}
    """.trimIndent()

    /** Returns a cleaned, guaranteed-valid result, or null (→ rules fallback). Lenient where safe. */
    private fun sanitizeAndValidate(raw: RawAiResult): AiResult? {
        val category = raw.category?.trim()?.uppercase()
        if (category == null || category !in VALID_CATEGORIES) return null

        val title = raw.title?.trim()?.let { if (it.length > 80) it.take(77) + "..." else it }
        if (title.isNullOrBlank()) return null

        val intent = raw.intent?.trim()?.takeIf { it.isNotBlank() }
            ?.let { if (it.length > 30) it.take(30) else it }
            ?: defaultIntentFor(category)

        val action = raw.action?.trim()?.uppercase()?.takeIf { it in VALID_ACTIONS }
            ?: defaultActionFor(category)

        return AiResult(
            category = category,
            intent = intent,
            title = title,
            summary = cleanOptional(raw.summary, 300),
            location = cleanOptional(raw.location, 80),
            date = cleanOptional(raw.date, 60),
            time = cleanOptional(raw.time, 40),
            url = cleanOptional(raw.url, 500),
            action = action
        )
    }

    private fun cleanOptional(value: String?, max: Int): String? =
        value?.trim()?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            ?.let { if (it.length > max) it.take(max) else it }

    private fun defaultActionFor(category: String) = when (category) {
        "PLACE" -> "OPEN_MAPS"
        "EVENT" -> "SET_REMINDER"
        "RECIPE", "TOPIC", "TOOL" -> "SEARCH"
        else -> "NONE"
    }

    private fun defaultIntentFor(category: String) = when (category) {
        "PLACE" -> "VISIT"
        "EVENT" -> "ATTEND"
        "RECIPE" -> "COOK"
        "TOOL" -> "TRY"
        "TOPIC" -> "LEARN"
        else -> "REMEMBER"
    }

    /** Raw shape used only for tolerant Gson parsing (all fields optional/nullable). */
    private data class RawAiResult(
        val category: String? = null,
        val intent: String? = null,
        val title: String? = null,
        val summary: String? = null,
        val location: String? = null,
        val date: String? = null,
        val time: String? = null,
        val url: String? = null,
        val action: String? = null
    )

    /** Validated result. category/intent/title/action are guaranteed non-null. */
    data class AiResult(
        val category: String,
        val intent: String,
        val title: String,
        val summary: String?,
        val location: String?,
        val date: String?,
        val time: String?,
        val url: String?,
        val action: String
    )

    companion object {
        private const val TAG = "AiIntentExtractor"
        private val VALID_CATEGORIES = setOf("PLACE", "EVENT", "RECIPE", "TOOL", "TOPIC", "OTHER")
        private val VALID_ACTIONS = setOf("OPEN_MAPS", "SET_REMINDER", "ADD_CALENDAR", "OPEN_URL", "SEARCH", "NONE")
        private val SAFETY_CATEGORIES = listOf(
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_DANGEROUS_CONTENT"
        )
    }
}
