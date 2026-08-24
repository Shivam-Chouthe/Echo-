package com.hackathon.echo.ocr

class Categorizer {
    fun categorize(text: String): CategoryInfo {
        val lowerText = text.lowercase()

        // Priority 1: Task detection (Action + Time/Task markers)
        if (isTask(lowerText)) return CategoryInfo("TASK", "Do this")
        
        // Priority 2: Other intents
        return when {
            isRecipe(lowerText) -> CategoryInfo("RECIPE", "Try this recipe")
            isEvent(lowerText) -> CategoryInfo("EVENT", "Attend this event")
            isTool(lowerText) -> CategoryInfo("TOOL", "Try this tool")
            isPlace(lowerText) -> CategoryInfo("PLACE", "Visit this place")
            isTopic(lowerText) -> CategoryInfo("TOPIC", "Learn/explore this")
            else -> CategoryInfo("NOTE", "Remember this")
        }
    }

    private fun isRecipe(text: String): Boolean {
        val keywords = listOf("recipe", "ingredients", "cook", "minutes", "bake", "cup", "tbsp", "tsp", "chicken", "pasta", "dish", "meal")
        return keywords.any { text.contains(it) }
    }

    private fun isEvent(text: String): Boolean {
        val keywords = listOf("event", "hackathon", "workshop", "conference", "meetup", "register", "tickets", "august", "september", "october", "november", "december", "pune", "mumbai", "bangalore")
        return keywords.any { text.contains(it) }
    }

    private fun isTool(text: String): Boolean {
        val keywords = listOf("tool", "app", "software", "ai", "website", "generator", "editor", "platform", "extension", "chrome", "github")
        return keywords.any { text.contains(it) }
    }

    private fun isPlace(text: String): Boolean {
        val keywords = listOf("visit", "cafe", "restaurant", "location", "travel", "hotel", "nearby", "address", "stay", "resort")
        return keywords.any { text.contains(it) }
    }

    private fun isTask(text: String): Boolean {
        val actionKeywords = listOf("buy", "submit", "finish", "complete", "call", "apply", "check", "do", "get", "pick up", "remember to")
        val timeKeywords = listOf("tomorrow", "today", "tonight", "on monday", "on tuesday", "on wednesday", "on thursday", "on friday", "on saturday", "on sunday", "by friday")
        val taskKeywords = listOf("todo", "task", "reminder")
        
        return actionKeywords.any { text.contains(it) } || timeKeywords.any { text.contains(it) } || taskKeywords.any { text.contains(it) }
    }

    private fun isTopic(text: String): Boolean {
        val keywords = listOf("learn", "how", "why", "what", "guide", "tutorial", "explained", "article", "paper", "concept")
        return keywords.any { text.contains(it) }
    }

    data class CategoryInfo(val category: String, val intent: String)
}
