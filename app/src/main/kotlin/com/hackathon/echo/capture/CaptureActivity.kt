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
        private const val AI_TIMEOUT_MS = 6_000L

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
        if (intent?.action == Intent.ACTION_SEND) {
            // Log full intent details for debugging Instagram/YouTube/Social shares
            Log.d("EchoCapture", "INTENT_DEBUG: Action=${intent.action}, Type=${intent.type}")
            intent.extras?.let { extras ->
                for (key in extras.keySet()) {
                    Log.d("EchoCapture", "INTENT_DEBUG: Extra[$key] = ${extras.get(key)}")
                }
            }

            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val stream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            
            captureScope.launch {
                if (text != null) {
                    // Route text directly to classification (Skips OCR)
                    val url = extractUrl(text)
                    saveToDatabase(text, "TEXT", url)
                } else if (stream != null) {
                    val extracted = ocrProcessor.extractText(stream)
                    if (extracted != null && extracted.isNotBlank()) {
                        saveToDatabase(extracted, "IMAGE", null)
                    } else {
                        Toast.makeText(this@CaptureActivity, "No text found in image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun extractUrl(text: String): String? {
        val urlRegex = "(https?://[\\w\\d.-]+\\.[a-z]{2,}[^\\s]*)"
        val pattern = java.util.regex.Pattern.compile(urlRegex, java.util.regex.Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private suspend fun saveToDatabase(content: String, type: String, sourceUrl: String?) {
        // OCR-noise cleanup is for screenshots only; shared text goes to the classifier as-received (trimmed).
        val processingContent = if (type == "IMAGE") {
            ocrProcessor.cleanOcrText(content).ifBlank { content }
        } else {
            content.trim()
        }

        // AI Layer Attempt — must never crash or hang the capture flow; any failure/timeout falls back to rules.
        val aiResult = try {
            withTimeoutOrNull(AI_TIMEOUT_MS) { aiIntentExtractor.extractIntent(processingContent) }
        } catch (e: CancellationException) {
            throw e // respect structured-concurrency cancellation; never swallow it
        } catch (e: Exception) {
            Log.e("EchoCapture", "AI layer threw; falling back to rules", e)
            null
        }
        
        val finalCategory: String
        val finalTitle: String
        val finalIntent: String
        val finalLocation: String?
        val finalDate: String?
        val finalSummary: String?
        val finalAction: String?
        val finalReminderAt: Long?
        val isAiRefined: Boolean

        if (aiResult != null) {
            finalCategory = aiResult.category
            finalTitle = aiResult.title
            finalIntent = aiResult.intent
            finalSummary = aiResult.summary
            finalAction = aiResult.action
            isAiRefined = true
            
            // Rules remain authoritative for location and date/reminders as per requirements
            val ruleEntities = entityExtractor.extract(processingContent)
            finalLocation = aiResult.location ?: ruleEntities.location
            finalDate = aiResult.date ?: ruleEntities.date
            finalReminderAt = ruleEntities.reminderAt
        } else {
            // Fallback to rules
            val categoryInfo = categorizer.categorize(processingContent)
            val entities = entityExtractor.extract(processingContent)
            
            finalCategory = categoryInfo.category
            finalTitle = entities.title
            finalIntent = categoryInfo.intent
            finalLocation = entities.location
            finalDate = entities.date
            finalSummary = null
            finalAction = null
            finalReminderAt = entities.reminderAt
            isAiRefined = false
        }

        val item = EchoItem(
            rawText = content,
            title = finalTitle,
            category = finalCategory,
            intent = finalIntent,
            summary = finalSummary,
            action = finalAction,
            date = finalDate,
            location = finalLocation,
            reminderAt = finalReminderAt,
            sourceType = type,
            source = null,
            sourceUrl = sourceUrl ?: aiResult?.url,
            isAiRefined = isAiRefined
        )
        
        val id = database.echoDao().insert(item)
        Log.d("EchoCapture", "SCHEDULER: Saved ${finalCategory} with ID: $id (AI: $isAiRefined)")
        
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
