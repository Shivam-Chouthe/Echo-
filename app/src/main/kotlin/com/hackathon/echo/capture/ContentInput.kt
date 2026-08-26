package com.hackathon.echo.capture

/**
 * Normalised representation of whatever the Android Share Sheet delivered, produced
 * by [CaptureActivity.handleIntent] before persistence.
 *
 * This is purely an INPUT model. It does NOT perform any understanding itself — the
 * actual intent/category derivation is done downstream by the deterministic
 * [com.hackathon.echo.ocr.Categorizer] (keyword rules) and
 * [com.hackathon.echo.ocr.EntityExtractor] (regex). There is no cloud/AI layer in the
 * project; [Url.fetchedMetadata] is the only network enrichment, and it comes from the
 * official YouTube Data API via [YouTubeMetadata]. If it is null, the pipeline falls
 * back to deterministic labelling. Keep this type free of side effects.
 */
sealed class ContentInput {

    /** Plain shared text (optionally containing a non-platform URL). */
    data class Text(
        val rawText: String,
        val url: String?
    ) : ContentInput()

    /** Text recognised from a shared image via on-device ML Kit OCR. */
    data class Image(
        val extractedText: String
    ) : ContentInput()

    /**
     * A shared platform URL (Instagram/YouTube/other). [rawText] is the full shared
     * payload (caption + link) preserved verbatim; [fetchedMetadata] is present only
     * when the official YouTube Data API returned usable snippet data, else null.
     */
    data class Url(
        val url: String,
        val rawText: String,
        val fetchedMetadata: YouTubeMetadata.YouTubeMeta?
    ) : ContentInput()
}
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
