package com.hackathon.echo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "echo_items")
data class EchoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawText: String,
    val title: String,
    val category: String, // RECIPE, EVENT, TOOL, PLACE, TOPIC, TASK, NOTE
    val intent: String,
    val summary: String? = null,
    val action: String? = null, // OPEN_MAPS, SET_REMINDER, ADD_CALENDAR, OPEN_URL, SEARCH, NONE
    val date: String? = null,
    val location: String? = null,
    val source: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderAt: Long? = null,
    val status: String = "PENDING", // PENDING, ECHOED, DONE
    val sourceType: String, // "TEXT" or "IMAGE"
    val sourceUrl: String? = null,
    val isAiRefined: Boolean = false
) {
    // Keep compatibility with old code if needed, but we'll migrate
    val content: String get() = rawText
}
