package com.hackathon.echo.capture

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Helper for the official YouTube Data API (videos.list).
 *
 * Responsibilities are strictly limited to:
 *   1. Extracting a video ID from common YouTube URL shapes.
 *   2. Fetching public snippet metadata (title/description/channel) for that ID.
 *
 * It does NOT download video/audio, scrape HTML, or use unofficial endpoints.
 * The API key is passed in from BuildConfig and is NEVER logged.
 */
object YouTubeMetadata {

    data class YouTubeMeta(
        val title: String,
        val description: String,
        val channelTitle: String
    )

    /**
     * Extracts a YouTube video ID from the supported URL formats:
     *  - https://youtube.com/shorts/VIDEO_ID
     *  - https://www.youtube.com/shorts/VIDEO_ID
     *  - https://youtu.be/VIDEO_ID
     *  - https://www.youtube.com/watch?v=VIDEO_ID
     *  - https://www.youtube.com/embed/VIDEO_ID
     * Returns null if no YouTube video ID can be found (e.g. an Instagram URL).
     * The original URL is never modified.
     */
    fun extractVideoId(url: String): String? {
        val patterns = listOf(
            Regex("youtu\\.be/([A-Za-z0-9_-]{6,})", RegexOption.IGNORE_CASE),
            Regex("youtube\\.com/shorts/([A-Za-z0-9_-]{6,})", RegexOption.IGNORE_CASE),
            Regex("youtube\\.com/embed/([A-Za-z0-9_-]{6,})", RegexOption.IGNORE_CASE),
            Regex("[?&]v=([A-Za-z0-9_-]{6,})", RegexOption.IGNORE_CASE)
        )
        for (p in patterns) {
            val id = p.find(url)?.groupValues?.getOrNull(1)
            if (!id.isNullOrBlank()) return id
        }
        return null
    }

    /**
     * Calls the official YouTube Data API videos.list endpoint for snippet metadata.
     * Returns null on any failure (blank key, network error, non-200, private/removed
     * video, empty result) so the caller can fall back safely. Never throws.
     *
     * The whole fetch is bounded by a hard 3-second budget via withTimeoutOrNull(3000):
     * on timeout it returns null and the caller proceeds to the deterministic fallback.
     * Socket timeouts are set below the budget so a stalled connection is abandoned too.
     */
    suspend fun fetchMetadata(videoId: String, apiKey: String): YouTubeMeta? {
        if (apiKey.isBlank()) {
            Log.d("EchoCapture", "YT: no API key configured - skipping metadata fetch")
            return null
        }
        return withTimeoutOrNull(3000L) {
            withContext(Dispatchers.IO) {
                var conn: HttpURLConnection? = null
                try {
                    val id = URLEncoder.encode(videoId, "UTF-8")
                    val endpoint = "https://www.googleapis.com/youtube/v3/videos" +
                        "?part=snippet&id=$id&key=$apiKey"
                    conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 2500
                        readTimeout = 2500
                    }
                    val code = conn.responseCode
                    if (code != HttpURLConnection.HTTP_OK) {
                        // Do not log the key or the full URL (it contains the key).
                        Log.w("EchoCapture", "YT: metadata request returned HTTP $code")
                        return@withContext null
                    }
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val items = JSONObject(body).optJSONArray("items")
                    if (items == null || items.length() == 0) {
                        Log.d("EchoCapture", "YT: no items returned for video")
                        return@withContext null
                    }
                    val snippet = items.getJSONObject(0).optJSONObject("snippet")
                        ?: return@withContext null
                    val title = snippet.optString("title").trim()
                    val description = snippet.optString("description").trim()
                    val channel = snippet.optString("channelTitle").trim()
                    if (title.isBlank() && description.isBlank()) {
                        return@withContext null
                    }
                    YouTubeMeta(title, description, channel)
                } catch (e: Exception) {
                    // Log the error type only, never the key/URL/response contents.
                    Log.w("EchoCapture", "YT: metadata fetch failed (${e.javaClass.simpleName})")
                    null
                } finally {
                    conn?.disconnect()
                }
            }
        }
    }

    /**
     * Best-effort metadata for a YouTube URL, key-optional.
     *  1. If an API key is present, tries the Data API (title + description + channel).
     *  2. Otherwise / on failure, falls back to the official key-free oEmbed endpoint
     *     (title + author_name; no description).
     * Returns null only if BOTH paths fail, so the caller can keep the raw URL.
     */
    suspend fun fetchBestMetadata(url: String, apiKey: String): YouTubeMeta? {
        val videoId = extractVideoId(url)
        if (videoId != null && apiKey.isNotBlank()) {
            val viaApi = fetchMetadata(videoId, apiKey)
            if (viaApi != null) return viaApi
        }
        return fetchOEmbed(url)
    }

    /**
     * Official YouTube oEmbed endpoint — requires NO API key. Returns title +
     * author_name (mapped to channelTitle); description is left blank (oEmbed does not
     * provide one). Bounded by a hard 3-second budget; never throws; never scrapes HTML.
     */
    suspend fun fetchOEmbed(url: String): YouTubeMeta? {
        Log.d("ECHO_DEBUG", "Fetching oEmbed metadata for: $url")
        return withTimeoutOrNull(3000L) {
            withContext(Dispatchers.IO) {
                var conn: HttpURLConnection? = null
                try {
                    val encoded = URLEncoder.encode(url, "UTF-8")
                    val endpoint = "https://www.youtube.com/oembed?url=$encoded&format=json"
                    conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 2500
                        readTimeout = 2500
                    }
                    val code = conn.responseCode
                    if (code != HttpURLConnection.HTTP_OK) {
                        Log.w("EchoCapture", "YT: oEmbed request returned HTTP $code")
                        Log.e("ECHO_DEBUG", "Metadata fetch failed/timed out: HTTP $code")
                        return@withContext null
                    }
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val title = json.optString("title").trim()
                    val author = json.optString("author_name").trim()
                    Log.d("ECHO_DEBUG", "Extracted Title: $title")
                    if (title.isBlank()) return@withContext null
                    YouTubeMeta(title = title, description = "", channelTitle = author)
                } catch (e: Exception) {
                    Log.w("EchoCapture", "YT: oEmbed fetch failed (${e.javaClass.simpleName})")
                    Log.e("ECHO_DEBUG", "Metadata fetch failed/timed out: ${e.message}")
                    null
                } finally {
                    conn?.disconnect()
                }
            }
        } ?: run {
            Log.e("ECHO_DEBUG", "Metadata fetch failed/timed out: exceeded 3000ms budget")
            null
        }
    }
}
