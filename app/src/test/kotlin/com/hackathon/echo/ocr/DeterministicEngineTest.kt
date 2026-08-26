package com.hackathon.echo.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the deterministic fallback fix for natural EVENT phrases with ordinal /
 * abbreviated / weekday dates, and guards the PLACE/RECIPE regressions.
 * Pure-JVM (no Android deps), so it runs under testDebugUnitTest.
 */
class DeterministicEngineTest {

    private val categorizer = Categorizer()
    private val entities = EntityExtractor()

    // --- The four required natural phrases ---

    @Test
    fun examsStartOn1stSep_isEvent_withNormalisedDate_andReminder() {
        val text = "exams start on 1st sep"
        assertEquals("EVENT", categorizer.categorize(text).category)
        val e = entities.extract(text)
        assertEquals("1 September", e.date)
        assertNotNull("reminder should be scheduled", e.reminderAt)
    }

    @Test
    fun examOnSeptember1_isEvent_withNormalisedDate() {
        val text = "exam on September 1"
        assertEquals("EVENT", categorizer.categorize(text).category)
        assertEquals("1 September", entities.extract(text).date)
    }

    @Test
    fun hackathonOn1stSeptember_isEvent_withNormalisedDate() {
        val text = "hackathon on 1st September"
        assertEquals("EVENT", categorizer.categorize(text).category)
        assertEquals("1 September", entities.extract(text).date)
    }

    @Test
    fun concertThisSaturday_isEvent_withWeekdayReminder() {
        val text = "concert this Saturday"
        assertEquals("EVENT", categorizer.categorize(text).category)
        val e = entities.extract(text)
        assertEquals("Saturday", e.date)
        assertNotNull("weekday reminder should be scheduled", e.reminderAt)
        assertTrue("reminder must be in the future", e.reminderAt!! > System.currentTimeMillis())
    }

    // --- Ordinal / abbreviation coverage ---

    @Test
    fun ordinalsAndAbbreviations_normaliseCorrectly() {
        assertEquals("22 September", entities.extract("meeting on 22nd sept").date)
        assertEquals("3 December", entities.extract("workshop on 3rd Dec").date)
        assertEquals("21 August", entities.extract("event on August 21st").date)
    }

    // --- Regression guards ---

    @Test
    fun placesToVisitInPune_staysPlace_notEvent() {
        // The old city->EVENT bug must NOT come back.
        assertEquals("PLACE", categorizer.categorize("5 places to visit in Pune").category)
    }

    @Test
    fun recipeStaysRecipe() {
        assertEquals("RECIPE", categorizer.categorize("chicken pasta recipe in 30 minutes").category)
    }

    @Test
    fun bareDatedSentenceWithoutEventNounOrVerb_isNotEvent() {
        // A date alone must not force EVENT.
        assertTrue(categorizer.categorize("photos from 1 September").category != "EVENT")
    }

    @Test
    fun cityAloneIsNotEvent() {
        assertTrue(categorizer.categorize("mumbai").category != "EVENT")
    }
}
