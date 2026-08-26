package com.hackathon.echo.capture

/** Where a shared piece of content came from. Stored on EchoItem.sourceType. */
enum class ContentSource { TEXT, IMAGE, YOUTUBE, URL }

/**
 * A normalized unit of shared content handed to the intelligence pipeline.
 *
 * One shared item = one intention (no multi-intention splitting for the MVP).
 *
 * @param rawText        the original, human-visible content (shown as "ORIGINAL CONTENT")
 * @param processingText the normalized/enriched text fed to Gemini and the rules engine
 * @param url            a preserved source URL when present (YouTube share, link, etc.)
 */
data class ContentInput(
    val source: ContentSource,
    val rawText: String,
    val processingText: String,
    val url: String? = null
)
