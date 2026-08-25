package com.hackathon.echo.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OcrProcessor(private val context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractText(imageUri: Uri): String? {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Conservative cleanup of OCR text to remove common UI noise
     * while preserving meaningful content for classification.
     */
    fun cleanOcrText(text: String): String {
        if (text.isBlank()) return ""
        
        val lines = text.lines()
            .map { it.trim() }
            .filter { line ->
                // 1. Filter out very short junk (single chars/symbols)
                if (line.length < 2) return@filter false
                
                // 2. Filter out purely time patterns (e.g. 12:30, 09:45 PM)
                val isTime = line.matches(Regex("\\d{1,2}:\\d{2}(\\s*[AP]M)?", RegexOption.IGNORE_CASE))
                if (isTime) return@filter false
                
                // 3. Filter out battery/percentage (e.g. 85%)
                val isPercentage = line.matches(Regex("\\d{1,3}%"))
                if (isPercentage) return@filter false
                
                // 4. Filter out common standalone UI buttons/labels that often appear in screenshots
                val uiLabels = listOf("back", "share", "settings", "done", "cancel", "edit", "save", "next", "previous")
                if (uiLabels.any { it.equals(line, ignoreCase = true) }) return@filter false
                
                true
            }
        
        // Join lines and collapse multiple empty lines to normalize structure
        return lines.joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
