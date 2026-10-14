package com.gallery.app.core.model

import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class SortOrderTest {

    private val mockUri = mock(Uri::class.java)

    private fun item(id: Long, name: String, date: Long, size: Long): MediaItem {
        return MediaItem(
            id = id,
            uri = mockUri,
            displayName = name,
            mimeType = "image/jpeg",
            width = 100,
            height = 100,
            size = size,
            dateAdded = date,
            dateModified = date,
            dateTaken = date
        )
    }

    @Test
    fun testDateDescSorting() {
        val i1 = item(1, "A.jpg", 1000L, 10L)
        val i2 = item(2, "B.jpg", 3000L, 20L)
        val i3 = item(3, "C.jpg", 2000L, 30L)

        val list = mutableListOf(i1, i2, i3)
        list.sortByDescending { it.dateTaken.takeIf { t -> t > 0 } ?: it.dateAdded }

        assertEquals(2L, list[0].id)
        assertEquals(3L, list[1].id)
        assertEquals(1L, list[2].id)
    }

    @Test
    fun testNameAscSorting() {
        val i1 = item(1, "zebra.jpg", 1000L, 10L)
        val i2 = item(2, "apple.jpg", 3000L, 20L)
        val i3 = item(3, "Banana.jpg", 2000L, 30L)

        val list = mutableListOf(i1, i2, i3)
        list.sortBy { it.displayName.lowercase() }

        assertEquals("apple.jpg", list[0].displayName)
        assertEquals("Banana.jpg", list[1].displayName)
        assertEquals("zebra.jpg", list[2].displayName)
    }

    @Test
    fun testSizeDescSorting() {
        val i1 = item(1, "A.jpg", 1000L, 500L)
        val i2 = item(2, "B.jpg", 2000L, 1500L)
        val i3 = item(3, "C.jpg", 3000L, 1000L)

        val list = mutableListOf(i1, i2, i3)
        list.sortByDescending { it.size }

        assertEquals(1500L, list[0].size)
        assertEquals(1000L, list[1].size)
        assertEquals(500L, list[2].size)
    }
}
