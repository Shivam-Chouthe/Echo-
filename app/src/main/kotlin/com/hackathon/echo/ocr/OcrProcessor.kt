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
    
    fun extractTextFromRaw(text: String): String {
        // For now just return as is, later we'll clean up links/captions
        return text
    }
}
