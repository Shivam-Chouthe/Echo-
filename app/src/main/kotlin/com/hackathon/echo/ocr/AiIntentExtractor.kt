package com.hackathon.echo.ocr

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.hackathon.echo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiIntentExtractor {
    private val gson = Gson()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.6-flash", // 1.5-flash & 2.0-flash are retired; live API points to 3.6-flash (verified via 404 response)
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.1f
            topK = 1
            topP = 1f
            maxOutputTokens = 500
            responseMimeType = "application/json"
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH),
        )
    )

    suspend fun extractIntent(text: String): AiResult? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            Log.w("AiIntentExtractor", "GEMINI_API_KEY is not set. Falling back to rules.")
            return@withContext null
        }

        val prompt = """
            You are Echo, a forgotten-intentions tracker.
            Analyze the following text captured from a user's screen or share sheet.
            Determine the user's intention and extract structured information.
            
            Categories: PLACE, EVENT, RECIPE, TOOL, TOPIC, OTHER
            Actions: OPEN_MAPS, SET_REMINDER, ADD_CALENDAR, OPEN_URL, SEARCH, NONE
            
            Classification Rules:
            - A city name (Pune, Mumbai, etc) alone is NOT an EVENT.
            - "Places to visit in [City]" is a PLACE with OPEN_MAPS action.
            - "Best cafes in [City]" is a PLACE with OPEN_MAPS action.
            - "Hackathon on [Date]" is an EVENT with SET_REMINDER action.
            - "Try this [Food] recipe" is a RECIPE with SEARCH action.
            
            Return JSON only in this schema:
            {
              "category": "String (One of: PLACE, EVENT, RECIPE, TOOL, TOPIC, OTHER)",
              "intent": "String (Short description of why the user saved this)",
              "title": "String (A clean, catchy title for the Echo item)",
              "summary": "String (A 1-sentence summary of the content)",
              "location": "String or null",
              "date": "String or null",
              "time": "String or null",
              "url": "String or null",
              "action": "String (One of: OPEN_MAPS, SET_REMINDER, ADD_CALENDAR, OPEN_URL, SEARCH, NONE)"
            }
            
            Text to analyze:
            $text
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val json = response.text ?: return@withContext null
            val result = gson.fromJson(json, AiResult::class.java)
            
            // Validate category and action
            if (isValidResult(result)) {
                result
            } else {
                Log.e("AiIntentExtractor", "Invalid AI result received: $json")
                null
            }
        } catch (e: Exception) {
            Log.e("AiIntentExtractor", "Gemini API call failed", e)
            null
        }
    }

    private fun isValidResult(result: AiResult): Boolean {
        val validCategories = listOf("PLACE", "EVENT", "RECIPE", "TOOL", "TOPIC", "OTHER")
        val validActions = listOf("OPEN_MAPS", "SET_REMINDER", "ADD_CALENDAR", "OPEN_URL", "SEARCH", "NONE")
        
        return result.category in validCategories && result.action in validActions
    }

    data class AiResult(
        val category: String,
        val intent: String,
        val title: String,
        val summary: String?,
        val location: String?,
        val date: String?,
        val time: String?,
        val url: String?,
        val action: String?
    )
}
