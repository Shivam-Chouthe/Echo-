package com.hackathon.echo.sync

import com.hackathon.echo.data.EchoItem
import java.text.SimpleDateFormat
import java.util.*

class LaptopDashboardGenerator {
    fun generateHtml(echoes: List<EchoItem>): String {
        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        
        val categories = listOf("EVENT", "RECIPE", "TASK", "TOOL", "PLACE", "TOPIC", "NOTE")
        val categorizedContent = categories.joinToString("\n") { category ->
            val items = echoes.filter { it.category == category }
            if (items.isNotEmpty()) {
                """
                <section class="category-section">
                    <h2 class="category-title">${getCategoryEmoji(category)} ${category}S</h2>
                    <div class="card-grid">
                        ${items.joinToString("\n") { generateCard(it) }}
                    </div>
                </section>
                """.trimIndent()
            } else ""
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Echo Dashboard</title>
            <style>
                :root {
                    --primary: #1B8A3F;
                    --bg: #FDFBF7;
                    --surface: #FFFFFF;
                    --text: #1A1C19;
                    --text-secondary: #6B6D6A;
                    --border: #E0E2DE;
                }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    background-color: var(--bg);
                    color: var(--text);
                    margin: 0;
                    padding: 40px;
                    line-height: 1.5;
                }
                header {
                    margin-bottom: 48px;
                    border-bottom: 2px solid var(--primary);
                    padding-bottom: 24px;
                }
                h1 { margin: 0; font-size: 40px; letter-spacing: -1px; color: var(--primary); }
                .subtitle { color: var(--text-secondary); margin-top: 8px; }
                .sync-time { font-size: 14px; color: var(--text-secondary); margin-top: 4px; }
                
                .category-section { margin-bottom: 48px; }
                .category-title { 
                    font-size: 14px; 
                    text-transform: uppercase; 
                    letter-spacing: 2px; 
                    color: var(--primary);
                    margin-bottom: 24px;
                    border-left: 4px solid var(--primary);
                    padding-left: 12px;
                }
                
                .card-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
                    gap: 24px;
                }
                
                .card {
                    background: var(--surface);
                    border: 1px solid var(--border);
                    border-radius: 12px;
                    padding: 24px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
                    transition: transform 0.2s;
                }
                .card:hover { transform: translateY(-4px); }
                
                .card-header { display: flex; justify-content: space-between; font-size: 12px; color: var(--text-secondary); margin-bottom: 12px; }
                .card-title { font-size: 20px; font-weight: 700; margin: 0 0 8px 0; }
                .card-intent { color: var(--text-secondary); font-style: italic; margin-bottom: 16px; }
                
                .entity-row { display: flex; align-items: center; gap: 8px; font-size: 14px; margin-bottom: 8px; }
                .entity-icon { font-size: 16px; }
                
                .status-tag { 
                    display: inline-block; 
                    padding: 4px 8px; 
                    border-radius: 4px; 
                    font-size: 12px; 
                    font-weight: 600; 
                    margin-top: 12px;
                }
                .status-scheduled { background: #E8F5E9; color: var(--primary); }
                .status-echoed { background: #F5F5F5; color: var(--text-secondary); }
                
                .raw-content {
                    margin-top: 20px;
                    padding-top: 16px;
                    border-top: 1px solid var(--border);
                    font-size: 13px;
                    color: var(--text-secondary);
                    white-space: pre-wrap;
                }
                
                .source-link {
                    display: inline-block;
                    margin-top: 16px;
                    color: var(--primary);
                    text-decoration: none;
                    font-weight: 600;
                    font-size: 14px;
                }
                .source-link:hover { text-decoration: underline; }
            </style>
        </head>
        <body>
            <header>
                <h1>ECHO</h1>
                <div class="subtitle">Your forgotten intentions, remembered.</div>
                <div class="sync-time">Last synced: $timestamp</div>
            </header>
            
            $categorizedContent
            
            <footer style="margin-top: 80px; text-align: center; color: var(--text-secondary); font-size: 14px;">
                Generated by Echo Android App
            </footer>
        </body>
        </html>
        """.trimIndent()
    }

    private fun generateCard(echo: EchoItem): String {
        val statusClass = if (echo.status == "ECHOED") "status-echoed" else "status-scheduled"
        val statusText = when {
            echo.status == "ECHOED" -> "🔔 Echoed"
            echo.reminderAt != null && echo.reminderAt > System.currentTimeMillis() -> "🔔 Echo scheduled"
            echo.date != null -> "Past date"
            else -> ""
        }
        
        val entities = StringBuilder()
        val dateTime = listOfNotNull(echo.date, echo.time).joinToString(" · ")
        if (dateTime.isNotEmpty()) {
            entities.append("""<div class="entity-row"><span class="entity-icon">📅</span> <span>$dateTime</span></div>""")
        }
        if (echo.location != null) {
            entities.append("""<div class="entity-row"><span class="entity-icon">📍</span> <span>${echo.location}</span></div>""")
        }

        val sourceLink = if (echo.sourceUrl != null) {
            """<a href="${echo.sourceUrl}" class="source-link" target="_blank">Open Original →</a>"""
        } else ""

        return """
        <div class="card">
            <div class="card-header">
                <span>${echo.category}</span>
                <span>${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(echo.createdAt))}</span>
            </div>
            <h3 class="card-title">${echo.title}</h3>
            <div class="card-intent">${echo.intent}</div>
            
            <div class="entities">
                $entities
            </div>
            
            ${if (statusText.isNotEmpty()) """<div class="status-tag $statusClass">$statusText</div>""" else ""}
            
            $sourceLink
            
            <div class="raw-content">${echo.rawText}</div>
        </div>
        """.trimIndent()
    }

    private fun getCategoryEmoji(category: String): String {
        return when (category) {
            "RECIPE" -> "🍳"
            "EVENT" -> "🎟️"
            "TOOL" -> "🛠️"
            "PLACE" -> "📍"
            "TOPIC" -> "📚"
            "TASK" -> "✅"
            else -> "📝"
        }
    }
}
