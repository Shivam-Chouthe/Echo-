package com.hackathon.echo.capture

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Enriches shared YouTube links (Shorts included) so the AI/rules layer has real textual
 * context instead of a bare URL. Three signals, in decreasing reliability, all keyless
 * (no OAuth, no API key, no download):
 *
 *   1. title       — YouTube oEmbed. Rock-solid, instant.
 *   2. description  — scraped from the watch page's ytInitialPlayerResponse.videoDetails.
 *   3. transcript   — BEST-EFFORT auto-caption text.
 *
 * IMPORTANT (verified Aug 2026): fetching caption *text* now requires a "PO token"
 * (proof-of-origin, from BotGuard/DroidGuard attestation) that a lightweight on-device
 * client cannot produce, so the transcript fetch almost always returns nothing today.
 * The code path is kept so it lights up automatically if YouTube relaxes this or a token
 * provider is added later — but the effective primary signal is title + description.
 *
 * Everything here is best-effort: any network/parse problem yields null for that signal
 * and the pipeline continues with whatever remains (down to the bare URL).
 */
object YouTubeEnricher {
    private const val TAG = "YouTubeEnricher"
    private const val BROWSER_UA =
        "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36"
    private const val MAX_DESCRIPTION_CHARS = 1500
    private const val MAX_TRANSCRIPT_CHARS = 3500

    /** Everything we managed to pull for a shared video. Any field may be null. */
    data class YtEnrichment(
        val title: String?,
        val description: String?,
        val transcript: String?
    )

    fun isYouTube(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val u = url.lowercase()
        return u.contains("youtube.com") || u.contains("youtu.be")
    }

    fun extractVideoId(url: String): String? {
        val patterns = listOf(
            Regex("""youtu\.be/([\w-]{6,})"""),
            Regex("""youtube\.com/shorts/([\w-]{6,})"""),
            Regex("""[?&]v=([\w-]{6,})"""),
            Regex("""youtube\.com/embed/([\w-]{6,})""")
        )
        for (p in patterns) {
            p.find(url)?.let { return it.groupValues[1] }
        }
        return null
    }

    /**
     * Full best-effort enrichment: title (oEmbed) + description & transcript (watch page).
     * Never throws; missing signals come back null.
     */
    suspend fun enrich(url: String): YtEnrichment = withContext(Dispatchers.IO) {
        val oembedTitle = fetchTitle(url) // fast + reliable; also our title fallback source
        val videoId = extractVideoId(url)
            ?: return@withContext YtEnrichment(oembedTitle, null, null)

        val player = fetchWatchPage(videoId)?.let { extractPlayerResponse(it) }
        val details = player?.optJSONObject("videoDetails")
        val pageTitle = details?.optString("title")?.takeIf { it.isNotBlank() }
        val description = details?.optString("shortDescription")
            ?.takeIf { it.isNotBlank() }
            ?.let { if (it.length > MAX_DESCRIPTION_CHARS) it.take(MAX_DESCRIPTION_CHARS) else it }
        val transcript = player?.let { pickCaptionBaseUrl(it) }?.let { fetchTranscript(it) }

        Log.d(TAG, "enrich videoId=$videoId title=${oembedTitle != null || pageTitle != null} " +
            "desc=${description != null} transcript=${transcript != null}")
        YtEnrichment(
            title = oembedTitle ?: pageTitle,
            description = description,
            transcript = transcript
        )
    }

    /**
     * Assembles the text handed to the intelligence pipeline, encoding the fallback order:
     * transcript (if any) is preferred over the description; the title is always prepended as
     * context. Sections are labeled so the AI can treat spoken transcript differently from
     * metadata. Falls back to the raw shared text when nothing was enriched.
     */
    fun buildProcessingText(e: YtEnrichment, fallback: String): String {
        val parts = mutableListOf<String>()
        e.title?.takeIf { it.isNotBlank() }?.let { parts += "VIDEO TITLE: $it" }
        when {
            !e.transcript.isNullOrBlank() -> parts += "VIDEO TRANSCRIPT: ${e.transcript}"
            !e.description.isNullOrBlank() -> parts += "VIDEO DESCRIPTION: ${e.description}"
        }
        return parts.joinToString("\n\n").ifBlank { fallback }
    }

    /** Best-effort video title via oEmbed. Never throws; returns null on any problem. */
    suspend fun fetchTitle(url: String): String? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val videoId = extractVideoId(url)
            val watchUrl = if (videoId != null) "https://www.youtube.com/watch?v=$videoId" else url
            val oembed = "https://www.youtube.com/oembed?url=" +
                URLEncoder.encode(watchUrl, "UTF-8") + "&format=json"

            conn = (URL(oembed).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "oEmbed HTTP ${conn.responseCode} — skipping title enrichment.")
                return@withContext null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val title = JSONObject(body).optString("title", "").trim()
            title.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w(TAG, "oEmbed enrichment failed (${e.javaClass.simpleName}) — skipping.")
            null
        } finally {
            conn?.disconnect()
        }
    }

    /** Downloads the watch-page HTML for a video. Null on any failure. */
    private fun fetchWatchPage(videoId: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            conn = (URL(watchUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 6000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", BROWSER_UA)
                setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "watch page HTTP ${conn.responseCode} — skipping desc/transcript.")
                return null
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.w(TAG, "watch page fetch failed (${e.javaClass.simpleName}) — skipping.")
            null
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Extracts the embedded `ytInitialPlayerResponse` JSON object from watch-page HTML by
     * brace-matching (string/escape aware) rather than a fragile regex. Returns null if the
     * marker is absent (e.g., a consent/redirect page) or the JSON won't parse.
     */
    private fun extractPlayerResponse(html: String): JSONObject? {
        val markers = listOf(
            "var ytInitialPlayerResponse = ",
            "ytInitialPlayerResponse = ",
            "ytInitialPlayerResponse ="
        )
        var start = -1
        for (m in markers) {
            val i = html.indexOf(m)
            if (i >= 0) { start = i + m.length; break }
        }
        if (start < 0) return null
        while (start < html.length && html[start] != '{') start++
        if (start >= html.length) return null

        var depth = 0
        var inStr = false
        var esc = false
        var i = start
        while (i < html.length) {
            val c = html[i]
            if (inStr) {
                when {
                    esc -> esc = false
                    c == '\\' -> esc = true
                    c == '"' -> inStr = false
                }
            } else {
                when (c) {
                    '"' -> inStr = true
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) {
                            return try {
                                JSONObject(html.substring(start, i + 1))
                            } catch (e: Exception) {
                                Log.w(TAG, "player response JSON parse failed — skipping.")
                                null
                            }
                        }
                    }
                }
            }
            i++
        }
        return null
    }

    /** Picks a caption track base URL, preferring English; null if the video has no captions. */
    private fun pickCaptionBaseUrl(player: JSONObject): String? {
        val tracks: JSONArray = player
            .optJSONObject("captions")
            ?.optJSONObject("playerCaptionsTracklistRenderer")
            ?.optJSONArray("captionTracks")
            ?: return null
        if (tracks.length() == 0) return null

        var firstUrl: String? = null
        for (i in 0 until tracks.length()) {
            val track = tracks.optJSONObject(i) ?: continue
            val base = track.optString("baseUrl", "").takeIf { it.isNotBlank() } ?: continue
            if (firstUrl == null) firstUrl = base
            if (track.optString("languageCode", "").startsWith("en", ignoreCase = true)) {
                return base
            }
        }
        return firstUrl
    }

    /**
     * Best-effort fetch + parse of caption text as JSON3. Returns null on the (currently
     * common) empty/blocked response, non-200, or unparseable body.
     */
    private fun fetchTranscript(baseUrl: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = if (baseUrl.contains("fmt=")) baseUrl else "$baseUrl&fmt=json3"
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 6000
                setRequestProperty("User-Agent", BROWSER_UA)
                setRequestProperty("Referer", "https://www.youtube.com/")
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "caption HTTP ${conn.responseCode} — no transcript (expected today).")
                return null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            if (body.isBlank()) {
                Log.d(TAG, "caption body empty — no transcript (PO-token wall, expected today).")
                return null
            }
            parseJson3Transcript(body)
        } catch (e: Exception) {
            Log.d(TAG, "transcript fetch failed (${e.javaClass.simpleName}) — no transcript.")
            null
        } finally {
            conn?.disconnect()
        }
    }

    /** Concatenates all `events[].segs[].utf8` into a single whitespace-normalized string. */
    private fun parseJson3Transcript(body: String): String? {
        return try {
            val events = JSONObject(body).optJSONArray("events") ?: return null
            val sb = StringBuilder()
            for (i in 0 until events.length()) {
                val segs = events.optJSONObject(i)?.optJSONArray("segs") ?: continue
                for (j in 0 until segs.length()) {
                    sb.append(segs.optJSONObject(j)?.optString("utf8", "").orEmpty())
                }
                sb.append(' ')
            }
            val text = sb.toString().replace(Regex("\\s+"), " ").trim()
            text.takeIf { it.isNotBlank() }
                ?.let { if (it.length > MAX_TRANSCRIPT_CHARS) it.take(MAX_TRANSCRIPT_CHARS) else it }
        } catch (e: Exception) {
            Log.d(TAG, "caption JSON3 parse failed — no transcript.")
            null
        }
    }
}
