package com.gallery.app.core.media

import com.gallery.app.core.model.MediaItem
import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.Calendar

class DateGrouperTest {

    private val mockUri = mock(Uri::class.java)

    private fun createMediaItem(id: Long, timestamp: Long): MediaItem {
        return MediaItem(
            id = id,
            uri = mockUri,
            displayName = "photo_$id.jpg",
            mimeType = "image/jpeg",
            width = 1920,
            height = 1080,
            size = 1024000L,
            dateAdded = timestamp,
            dateModified = timestamp,
            dateTaken = timestamp
        )
    }

    @Test
    fun testGroupByDate_todayAndYesterday() {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterday = calendar.timeInMillis

        val item1 = createMediaItem(1, now)
        val item2 = createMediaItem(2, yesterday)

        val groups = DateGrouper.groupByDate(listOf(item1, item2))

        assertEquals(2, groups.size)
        assertEquals("Today", groups[0].label)
        assertEquals("Yesterday", groups[1].label)
        assertEquals(1, groups[0].items.size)
        assertEquals(1, groups[1].items.size)
    }

    @Test
    fun testGroupByDate_emptyList() {
        val groups = DateGrouper.groupByDate(emptyList())
        assertTrue(groups.isEmpty())
    }

    @Test
    fun testGroupByDate_multipleItemsSameDay() {
        val now = System.currentTimeMillis()
        val item1 = createMediaItem(1, now)
        val item2 = createMediaItem(2, now - 1000)

        val groups = DateGrouper.groupByDate(listOf(item1, item2))

        assertEquals(1, groups.size)
        assertEquals("Today", groups[0].label)
        assertEquals(2, groups[0].items.size)
    }
}
