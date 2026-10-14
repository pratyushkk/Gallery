package com.gallery.app.feature.search

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class SearchLogicTest {

    private fun isMonthQuery(q: String): Boolean {
        val months = listOf(
            "january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november", "december",
            "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "oct", "nov", "dec"
        )
        return q.lowercase().trim() in months
    }

    private fun getMonthNumber(q: String): Int {
        val monthMap = mapOf(
            "january" to 0, "jan" to 0, "february" to 1, "feb" to 1,
            "march" to 2, "mar" to 2, "april" to 3, "apr" to 3,
            "may" to 4, "june" to 5, "jun" to 5, "july" to 6, "jul" to 6,
            "august" to 7, "aug" to 7, "september" to 8, "sep" to 8,
            "october" to 9, "oct" to 9, "november" to 10, "nov" to 10,
            "december" to 11, "dec" to 11
        )
        return monthMap[q.lowercase().trim()] ?: -1
    }

    @Test
    fun testMonthDetection() {
        assertTrue(isMonthQuery("January"))
        assertTrue(isMonthQuery("dec"))
        assertTrue(isMonthQuery("october"))
        assertFalse(isMonthQuery("summer"))
        assertFalse(isMonthQuery("photo"))
    }

    @Test
    fun testMonthNumberMapping() {
        assertEquals(0, getMonthNumber("january"))
        assertEquals(9, getMonthNumber("october"))
        assertEquals(11, getMonthNumber("december"))
        assertEquals(-1, getMonthNumber("invalid"))
    }
}
