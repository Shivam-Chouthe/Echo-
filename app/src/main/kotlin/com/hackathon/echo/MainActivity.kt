package com.hackathon.echo

import android.Manifest
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.echo.data.EchoItem
import com.hackathon.echo.sync.LaptopDashboardGenerator
import com.hackathon.echo.ui.EchoViewModel
import com.hackathon.echo.ui.theme.EchoTheme
import com.hackathon.echo.ui.theme.PrimaryGreen
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val startId = intent.getIntExtra("echo_id", -1)
        
        setContent {
            EchoTheme {
                NotificationPermissionEffect()
                
                var selectedEchoId by remember { mutableStateOf(if (startId != -1) startId else null as Int?) }
                
                if (selectedEchoId == null) {
                    DashboardScreen(onEchoClick = { selectedEchoId = it })
                } else {
                    EchoDetailScreen(echoId = selectedEchoId!!, onBack = { selectedEchoId = null })
                }
            }
        }
    }
}

@Composable
fun NotificationPermissionEffect() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Handle result if needed
        }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: EchoViewModel = viewModel(), onEchoClick: (Int) -> Unit) {
    val echoes by viewModel.echoes.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ECHOES",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (echoes.isEmpty()) {
                            Toast.makeText(context, "No Echoes to sync", Toast.LENGTH_SHORT).show()
                        } else {
                            syncToPc(context, echoes)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Sync to PC",
                            tint = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (echoes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Share something to Echo to see it here",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(echoes, key = { it.id }) { echo ->
                    EchoCard(
                        echo = echo,
                        onDelete = { viewModel.deleteEcho(echo) },
                        onClick = { onEchoClick(echo.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EchoCard(echo: EchoItem, onDelete: () -> Unit, onClick: () -> Unit) {
    val isDone = echo.status == "DONE"
    val cardAlpha = if (isDone) 0.6f else 1.0f
    
    val categoryIcon = when (echo.category) {
        "RECIPE" -> "🍳"
        "EVENT" -> "🎟️"
        "TOOL" -> "🛠️"
        "PLACE" -> "📍"
        "TOPIC" -> "📚"
        "TASK" -> "✅"
        else -> "📝"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDone) "✓ DONE" else "$categoryIcon ${echo.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDone) Color.Gray else PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = DateUtils.getRelativeTimeSpanString(echo.createdAt).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = echo.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDone) Color.Gray else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )

            Text(
                text = echo.intent,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            if (echo.date != null || echo.location != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (echo.date != null) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Date",
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryGreen
                        )
                        Text(
                            text = echo.date,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                        )
                    }
                    if (echo.location != null) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location",
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryGreen
                        )
                        Text(
                            text = echo.location,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Scheduling Indicator
            val isFuture = echo.reminderAt != null && echo.reminderAt > System.currentTimeMillis()
            val isPastDate = echo.date != null && (echo.reminderAt == null && echo.status == "PENDING")

            if (isFuture || echo.status == "ECHOED" || isPastDate) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPastDate) Icons.Default.Event else Icons.Default.NotificationsActive,
                        contentDescription = "Echo Status",
                        modifier = Modifier.size(12.dp),
                        tint = if (echo.status == "ECHOED" || isPastDate) Color.Gray else PrimaryGreen
                    )
                    Text(
                        text = when {
                            echo.status == "ECHOED" -> "Echoed"
                            isFuture -> "Echo scheduled"
                            isPastDate -> "Past date"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (echo.status == "ECHOED" || isPastDate) Color.Gray else PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = echo.rawText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray.copy(alpha = 0.8f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EchoDetailScreen(echoId: Int, viewModel: EchoViewModel = viewModel(), onBack: () -> Unit) {
    val echoes by viewModel.echoes.collectAsState()
    val echo = echoes.find { it.id == echoId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ECHO DETAIL") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (echo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Echo not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = echo.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = echo.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = echo.intent,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (echo.date != null) {
                    DetailItem(icon = Icons.Default.Event, label = "Date", value = echo.date)
                }
                if (echo.location != null) {
                    DetailItem(icon = Icons.Default.Place, label = "Location", value = echo.location)
                }
                
                val statusText = when {
                    echo.status == "ECHOED" -> "Echoed"
                    echo.reminderAt != null && echo.reminderAt > System.currentTimeMillis() -> "Echo scheduled"
                    else -> "No active echo"
                }
                DetailItem(icon = Icons.Default.NotificationsActive, label = "Status", value = statusText)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "ORIGINAL CONTENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = echo.rawText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "SUGGESTED ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                val context = LocalContext.current
                
                when (echo.category) {
                    "EVENT" -> {
                        if (echo.date != null) {
                            ActionButton(
                                text = "Add to Calendar",
                                icon = Icons.Default.Event,
                                onClick = { addToCalendar(context, echo) }
                            )
                        }
                    }
                    "PLACE" -> {
                        if (echo.location != null) {
                            ActionButton(
                                text = "Open in Maps",
                                icon = Icons.Default.Place,
                                onClick = { openInMaps(context, echo.location) }
                            )
                        }
                    }
                    "RECIPE" -> {
                        ActionButton(
                            text = "Search Recipe",
                            icon = Icons.Default.Search,
                            onClick = { searchOnWeb(context, "${echo.title} recipe") }
                        )
                        ActionButton(
                            text = "Copy Content",
                            icon = Icons.Default.ContentCopy,
                            onClick = { copyToClipboard(context, echo.rawText) }
                        )
                    }
                    "TOPIC" -> {
                        ActionButton(
                            text = "Explore Topic",
                            icon = Icons.Default.Search,
                            onClick = { searchOnWeb(context, echo.title) }
                        )
                        ActionButton(
                            text = "Copy Content",
                            icon = Icons.Default.ContentCopy,
                            onClick = { copyToClipboard(context, echo.rawText) }
                        )
                    }
                    "TOOL" -> {
                        if (echo.sourceUrl != null) {
                            ActionButton(
                                text = "Open Tool",
                                icon = Icons.Default.Language,
                                onClick = { openUrl(context, echo.sourceUrl) }
                            )
                        } else {
                            ActionButton(
                                text = "Search Tool",
                                icon = Icons.Default.Search,
                                onClick = { searchOnWeb(context, echo.title) }
                            )
                        }
                    }
                    "TASK" -> {
                        if (echo.status != "DONE") {
                            ActionButton(
                                text = "Mark Done",
                                icon = Icons.Default.Check,
                                onClick = { viewModel.updateEchoStatus(echo, "DONE") }
                            )
                        } else {
                            Text("✓ This task is complete", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    else -> {
                        ActionButton(
                            text = "Copy to Clipboard",
                            icon = Icons.Default.ContentCopy,
                            onClick = { copyToClipboard(context, echo.rawText) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

fun addToCalendar(context: Context, echo: EchoItem) {
    try {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, echo.title)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, echo.location)
            .putExtra(CalendarContract.Events.DESCRIPTION, "Saved via Echo: ${echo.intent}\n\n${echo.rawText}")
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        
        // We'd ideally parse echo.date properly here, but for MVP pre-filling is key
        // The user will confirm the date in the Calendar UI anyway.

        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open Calendar", Toast.LENGTH_SHORT).show()
    }
}

fun openInMaps(context: Context, location: String) {
    if (location.isBlank()) return
    
    val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
    
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback to browser Google Maps
        try {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(location)}")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open Maps or Browser", Toast.LENGTH_SHORT).show()
        }
    }
}

fun searchOnWeb(context: Context, query: String) {
    try {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, query)
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}"))
        context.startActivity(intent)
    }
}

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Echo Content", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

fun syncToPc(context: android.content.Context, echoes: List<EchoItem>) {
    try {
        val generator = LaptopDashboardGenerator()
        val html = generator.generateHtml(echoes)
        
        val file = File(context.cacheDir, "echo_dashboard.html")
        FileOutputStream(file).use { 
            it.write(html.toByteArray())
        }
        
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "com.hackathon.echo.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Sync Echo Dashboard to PC"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to generate dashboard: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
