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
import androidx.lifecycle.lifecycleScope
import com.hackathon.echo.data.AppDatabase
import com.hackathon.echo.data.EchoItem
import com.hackathon.echo.notifications.EchoAlarmReceiver
import com.hackathon.echo.ocr.Categorizer
import com.hackathon.echo.ocr.EntityExtractor
import com.hackathon.echo.ocr.OcrProcessor
import com.hackathon.echo.ui.theme.EchoTheme
import com.hackathon.echo.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CaptureActivity : ComponentActivity() {
    private val ocrProcessor by lazy { OcrProcessor(this) }
    private val categorizer by lazy { Categorizer() }
    private val entityExtractor by lazy { EntityExtractor() }
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }

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
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val stream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            
            lifecycleScope.launch {
                if (text != null) {
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
        val categoryInfo = categorizer.categorize(content)
        val entities = entityExtractor.extract(content)

        val item = EchoItem(
            rawText = content,
            title = entities.title,
            category = categoryInfo.category,
            intent = categoryInfo.intent,
            date = entities.date,
            location = entities.location,
            reminderAt = entities.reminderAt,
            sourceType = type,
            source = null,
            sourceUrl = sourceUrl
        )
        
        val id = database.echoDao().insert(item)
        Log.d("EchoCapture", "SCHEDULER: Saved ${categoryInfo.category} with ID: $id")
        Log.d("EchoCapture", "SCHEDULER: reminderAt: ${entities.reminderAt}, current: ${System.currentTimeMillis()}")
        
        entities.reminderAt?.let { reminderTime ->
            if (reminderTime > System.currentTimeMillis()) {
                Log.d("EchoCapture", "SCHEDULER: Scheduling alarm for ID: $id at $reminderTime")
                scheduleAlarm(id.toInt(), reminderTime)
            } else {
                Log.d("EchoCapture", "SCHEDULER: Not scheduling - reminderAt is in the past")
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
