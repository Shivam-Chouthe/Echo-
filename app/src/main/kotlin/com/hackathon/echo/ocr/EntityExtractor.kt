package com.hackathon.echo.ocr

import java.util.*
import java.util.regex.Pattern

class EntityExtractor {
    fun extract(text: String): ExtractedEntities {
        val title = extractTitle(text)
        val dateStr = extractDate(text)
        val location = extractLocation(text)
        val reminderAt = dateStr?.let { calculateReminder(it) }
        
        return ExtractedEntities(title, dateStr, location, reminderAt)
    }

    private fun extractTitle(text: String): String {
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return "Untitled Echo"
        
        // Take the first line as a potential title
        var title = lines[0].trim()
        
        // Stop at common delimiters that separate title from details
        val delimiters = listOf(".", ",", "-", "—", " at ", " on ", " starts ", " ends ")
        for (delim in delimiters) {
            val index = title.indexOf(delim, ignoreCase = true)
            if (index != -1 && index > 3) { // Only split if title has some substance
                title = title.substring(0, index).trim()
            }
        }

        // Strip common date patterns from title as a secondary cleanup
        val datePatterns = listOf(
            "\\d{1,2}\\s+(?:January|February|March|April|May|June|July|August|September|October|November|December)",
            "\\d{1,2}/\\d{1,2}(?:/\\d{2,4})?",
            "\\d{4}-\\d{2}-\\d{2}",
            "tomorrow",
            "today"
        )
        for (pattern in datePatterns) {
            title = title.replace(Regex(pattern, RegexOption.IGNORE_CASE), "").trim()
        }
        
        // Strip common locations
        val locations = listOf("Pune", "Mumbai", "Bangalore", "Delhi", "Hyderabad", "Chennai", "Kolkata", "Koregaon Park", "Baner", "Hinjewadi")
        for (loc in locations) {
            title = title.replace(Regex(loc, RegexOption.IGNORE_CASE), "").trim()
        }
        
        // Strip times like "10 AM", "14:00"
        title = title.replace(Regex("\\d{1,2}(?::\\d{2})?\\s*(?:AM|PM)", RegexOption.IGNORE_CASE), "").trim()
        
        // Strip leading/trailing punctuation and extra spaces
        title = title.replace(Regex("^[^\\w]+|[^\\w]+\$"), "").trim()
        title = title.replace(Regex("\\s+"), " ")

        if (title.isBlank() || title.length < 2) return "Untitled Echo"
        return if (title.length > 40) title.take(37) + "..." else title
    }

    private fun extractDate(text: String): String? {
        // Date is only meaningful if preceded by intent markers or if it's a primary focal point
        val intentMarkers = listOf("on ", "at ", "by ", "—", "date:", "event", "task", "deadline")
        val lowerText = text.lowercase()
        
        // Regex for dates
        val datePatterns = listOf(
            "\\d{1,2}\\s+(?:January|February|March|April|May|June|July|August|September|October|November|December)",
            "\\d{1,2}/\\d{1,2}(?:/\\d{2,4})?",
            "\\d{4}-\\d{2}-\\d{2}"
        )
        
        for (patternStr in datePatterns) {
            val pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val foundDate = matcher.group()
                val datePos = matcher.start()
                
                // Strict check: is it in the first line or preceded by a marker?
                val isFirstLine = text.lines().firstOrNull()?.contains(foundDate) == true
                val context = if (datePos > 15) lowerText.substring(datePos - 15, datePos) else lowerText.substring(0, datePos)
                val hasMarker = intentMarkers.any { context.contains(it) }
                
                if (hasMarker || isFirstLine) {
                    return foundDate
                }
            }
        }
        
        // Check for relative dates - only if it looks like a task or explicit reminder
        val relativeDateMarkers = listOf("tomorrow", "today", "tonight")
        for (marker in relativeDateMarkers) {
            if (lowerText.contains(marker)) {
                // Check if it's part of a task/action
                val actionKeywords = listOf("buy", "submit", "call", "do", "get", "remind", "finish")
                if (actionKeywords.any { lowerText.contains(it) } || lowerText.length < 50) {
                    return marker.replaceFirstChar { it.uppercase() }
                }
            }
        }
        
        return null
    }

    private fun extractLocation(text: String): String? {
        val locations = listOf("Pune", "Mumbai", "Bangalore", "Delhi", "Hyderabad", "Chennai", "Kolkata", "Koregaon Park", "Baner", "Hinjewadi")
        for (loc in locations) {
            if (text.contains(loc, ignoreCase = true)) {
                return loc
            }
        }
        
        // Regex for "at [Place]" or "in [Place]"
        val placeRegex = Pattern.compile("(?:at|in)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)")
        val matcher = placeRegex.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)
        }
        
        return null
    }

    private fun calculateReminder(dateStr: String): Long? {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        return when {
            dateStr.equals("Tomorrow", ignoreCase = true) -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 9) // 9 AM tomorrow
                calendar.set(Calendar.MINUTE, 0)
                calendar.timeInMillis
            }
            dateStr.equals("Today", ignoreCase = true) -> {
                calendar.timeInMillis + 10000 // Trigger in 10 seconds for demo
            }
            else -> {
                // Try parsing month-based dates
                val monthPattern = Pattern.compile("(\\d{1,2})\\s+(January|February|March|April|May|June|July|August|September|October|November|December)", Pattern.CASE_INSENSITIVE)
                val matcher = monthPattern.matcher(dateStr)
                if (matcher.find()) {
                    val day = matcher.group(1)?.toIntOrNull() ?: return null
                    val monthName = matcher.group(2)?.lowercase() ?: return null
                    val month = when (monthName) {
                        "january" -> Calendar.JANUARY
                        "february" -> Calendar.FEBRUARY
                        "march" -> Calendar.MARCH
                        "april" -> Calendar.APRIL
                        "may" -> Calendar.MAY
                        "june" -> Calendar.JUNE
                        "july" -> Calendar.JULY
                        "august" -> Calendar.AUGUST
                        "september" -> Calendar.SEPTEMBER
                        "october" -> Calendar.OCTOBER
                        "november" -> Calendar.NOVEMBER
                        "december" -> Calendar.DECEMBER
                        else -> -1
                    }
                    if (month != -1) {
                        calendar.set(currentYear, month, day, 9, 0)
                        
                        val eventTime = calendar.timeInMillis
                        val now = System.currentTimeMillis()

                        return when {
                            // If it's a future event (more than 24h away), schedule for 1 day before
                            eventTime > now + 86400000 -> {
                                eventTime - 86400000 // 1 day before
                            }
                            // If it's today or less than 24h away, but still in future
                            eventTime > now -> {
                                // Schedule for 60 seconds from now for demo/testing as requested
                                now + 60000 
                            }
                            else -> null // Truly in the past
                        }
                    } else null
                } else null
            }
        }
    }

    data class ExtractedEntities(
        val title: String,
        val date: String?,
        val location: String?,
        val reminderAt: Long?
    )
}
