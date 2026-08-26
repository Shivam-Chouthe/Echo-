package com.hackathon.echo.capture

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.echo.data.AppDatabase
import com.hackathon.echo.data.EchoItem
import com.hackathon.echo.notifications.EchoAlarmReceiver
import com.hackathon.echo.ocr.AiIntentExtractor
import com.hackathon.echo.ocr.Categorizer
import com.hackathon.echo.ocr.EntityExtractor
import com.hackathon.echo.ocr.OcrProcessor
import com.hackathon.echo.ui.theme.EchoTheme
import com.hackathon.echo.ui.theme.PrimaryGreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class CaptureActivity : ComponentActivity() {
    private val ocrProcessor by lazy { OcrProcessor(this) }
    private val categorizer by lazy { Categorizer() }
    private val entityExtractor by lazy { EntityExtractor() }
    private val aiIntentExtractor by lazy { AiIntentExtractor() }
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    companion object {
        private const val AI_TIMEOUT_MS = 10_000L

        // Capture/save must complete even after the transparent overlay calls finish(),
        // so it runs on a process-scoped job instead of the Activity's lifecycleScope.
        private val captureScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)

        setContent {
            EchoTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EchoCaptureOverlay {
                        finish()
                    }
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return

        // Log intent details for debugging YouTube / social / screenshot shares.
        Log.d("EchoCapture", "INTENT_DEBUG: Action=${intent.action}, Type=${intent.type}")
        intent.extras?.let { extras ->
            for (key in extras.keySet()) {
                Log.d("EchoCapture", "INTENT_DEBUG: Extra[$key] = ${extras.get(key)}")
            }
        }

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val stream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        captureScope.launch {
            val input = buildContentInput(sharedText, stream)
            if (input == null) {
                Toast.makeText(this@CaptureActivity, "Nothing to save", Toast.LENGTH_SHORT).show()
                return@launch
            }
            processAndSave(input)
        }
    }

    /**
     * Normalizes a share into a single ContentInput (one share = one intention).
     * Text/URL/YouTube skip OCR; images run ML Kit OCR first. YouTube links are enriched
     * (best-effort) with the video title so the AI/rules layer has real context.
     */
    private suspend fun buildContentInput(sharedText: String?, stream: Uri?): ContentInput? {
        if (!sharedText.isNullOrBlank()) {
            val text = sharedText.trim()
            val url = extractUrl(text)
            return when {
                YouTubeEnricher.isYouTube(url) -> {
                    val enrichment = YouTubeEnricher.enrich(url!!)
                    val processing = YouTubeEnricher.buildProcessingText(enrichment, text)
                    Log.d("EchoCapture", "YOUTUBE share; title=${enrichment.title != null} " +
                        "desc=${enrichment.description != null} transcript=${enrichment.transcript != null}")
                    ContentInput(ContentSource.YOUTUBE, rawText = text, processingText = processing, url = url)
                }
                url != null -> ContentInput(ContentSource.URL, rawText = text, processingText = text, url = url)
                else -> ContentInput(ContentSource.TEXT, rawText = text, processingText = text)
            }
        }

        if (stream != null) {
            val extracted = ocrProcessor.extractText(stream)
            if (extracted.isNullOrBlank()) return null
            val cleaned = ocrProcessor.cleanOcrText(extracted).ifBlank { extracted }
            return ContentInput(
                source = ContentSource.IMAGE,
                rawText = extracted,
                processingText = cleaned,
                url = extractUrl(extracted)
            )
        }

        return null
    }

    private fun extractUrl(text: String): String? {
        val urlRegex = "(https?://[\\w\\d.-]+\\.[a-z]{2,}[^\\s]*)"
        val pattern = java.util.regex.Pattern.compile(urlRegex, java.util.regex.Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private suspend fun processAndSave(input: ContentInput) {
        val processingContent = input.processingText

        // AI Layer — must never crash or hang the capture flow; any failure/timeout falls back to rules.
        val aiResult = try {
            withTimeoutOrNull(AI_TIMEOUT_MS) { aiIntentExtractor.extractIntent(processingContent) }
        } catch (e: CancellationException) {
            throw e // respect structured-concurrency cancellation; never swallow it
        } catch (e: Exception) {
            Log.e("EchoCapture", "AI layer threw; falling back to rules", e)
            null
        }

        // Rules remain authoritative for location, date, and reminders.
        val ruleEntities = entityExtractor.extract(processingContent)

        val finalCategory: String
        val finalTitle: String
        val finalIntent: String
        val finalLocation: String?
        val finalDate: String?
        val finalTime: String?
        val finalSummary: String?
        val finalAction: String?
        val finalReminderAt: Long?
        val isAiRefined: Boolean

        if (aiResult != null) {
            // AI is authoritative for the descriptive fields.
            finalCategory = aiResult.category
            finalTitle = aiResult.title
            finalIntent = aiResult.intent
            finalSummary = aiResult.summary
            finalAction = aiResult.action
            // Deterministic entities win for location/date/reminders (prevents hallucinated reminders).
            finalLocation = aiResult.location ?: ruleEntities.location
            finalDate = aiResult.date ?: ruleEntities.date
            finalTime = aiResult.time
            finalReminderAt = ruleEntities.reminderAt
            isAiRefined = true
        } else {
            // Guaranteed deterministic fallback.
            val categoryInfo = categorizer.categorize(processingContent)
            finalCategory = categoryInfo.category
            finalTitle = ruleEntities.title
            finalIntent = categoryInfo.intent
            finalLocation = ruleEntities.location
            finalDate = ruleEntities.date
            finalTime = null
            finalSummary = null
            finalAction = null
            finalReminderAt = ruleEntities.reminderAt
            isAiRefined = false
        }

        // Bare-URL safety net (fallback chain, final level): a shared link that produced no
        // usable entities and no meaningful category is still saved as an openable item
        // (category OTHER / action OPEN_URL) rather than a dead, empty card.
        val hasEntities = finalDate != null || finalLocation != null
        val unclassified = finalCategory == "OTHER" || finalCategory == "NOTE"
        val bareUrlFallback = input.url != null && !hasEntities && unclassified
        val savedCategory = if (bareUrlFallback) "OTHER" else finalCategory
        val savedAction = if (bareUrlFallback) "OPEN_URL" else finalAction
        if (bareUrlFallback) Log.d("EchoCapture", "Bare-URL fallback → OTHER/OPEN_URL for ${input.source}")

        val item = EchoItem(
            rawText = input.rawText,
            title = finalTitle,
            category = savedCategory,
            intent = finalIntent,
            summary = finalSummary,
            action = savedAction,
            date = finalDate,
            time = finalTime,
            location = finalLocation,
            reminderAt = finalReminderAt,
            sourceType = input.source.name,
            source = null,
            sourceUrl = input.url ?: aiResult?.url,
            isAiRefined = isAiRefined
        )

        val id = database.echoDao().insert(item)
        Log.d("EchoCapture", "SAVED $finalCategory/$finalIntent id=$id (AI=$isAiRefined, src=${input.source})")

        finalReminderAt?.let { reminderTime ->
            if (reminderTime > System.currentTimeMillis()) {
                scheduleAlarm(id.toInt(), reminderTime)
            }
        }
    }

    private fun scheduleAlarm(echoId: Int, reminderTime: Long) {
        val intent = Intent(this, EchoAlarmReceiver::class.java).apply {
            putExtra("echo_id", echoId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, echoId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e("EchoCapture", "SCHEDULER: Cannot schedule exact alarms - permission missing")
                    // Fallback to inexact or just log
                }
            }
            
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
            Log.d("EchoCapture", "SCHEDULER: Successfully registered alarm for ID: $echoId")
        } catch (e: Exception) {
            Log.e("EchoCapture", "SCHEDULER: Failed to schedule alarm", e)
        }
    }
}

@Composable
fun EchoCaptureOverlay(onComplete: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    var showSaved by remember { mutableStateOf(false) }
    
    val radius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        delay(1500) // Show pulse for 1.5s
        showSaved = true
        delay(800) // Show "Saved" for 0.8s
        onComplete()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!showSaved) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    // Inner dot
                    drawCircle(
                        color = PrimaryGreen,
                        radius = 12.dp.toPx()
                    )
                    
                    // Concentric wave
                    drawCircle(
                        color = PrimaryGreen.copy(alpha = alpha),
                        radius = radius.dp.toPx(),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        } else {
            Text(
                text = "SAVED",
                color = PrimaryGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }
    }
}
