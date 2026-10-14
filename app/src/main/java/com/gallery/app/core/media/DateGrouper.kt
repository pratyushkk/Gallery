package com.gallery.app.core.media

import com.gallery.app.core.model.DateGroup
import com.gallery.app.core.model.MediaItem
import com.gallery.app.core.model.SortOrder
import java.text.SimpleDateFormat
import java.util.*

object DateGrouper {

    fun groupBySortOrder(items: List<MediaItem>, sortOrder: SortOrder): List<DateGroup> {
        if (items.isEmpty()) return emptyList()
        return when (sortOrder) {
            SortOrder.DATE_DESC -> groupByDate(items, descending = true)
            SortOrder.DATE_ASC -> groupByDate(items, descending = false)
            SortOrder.NAME_ASC -> groupByName(items, ascending = true)
            SortOrder.NAME_DESC -> groupByName(items, ascending = false)
            SortOrder.SIZE_DESC -> groupBySize(items, descending = true)
            SortOrder.SIZE_ASC -> groupBySize(items, descending = false)
        }
    }

    fun groupByDate(items: List<MediaItem>, descending: Boolean = true): List<DateGroup> {
        if (items.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val thisWeekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val groups = LinkedHashMap<String, MutableList<MediaItem>>()
        val groupDates = HashMap<String, Long>()

        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val dayMonthFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

        val dayLabelCache = HashMap<Long, String>()

        for (item in items) {
            val timestamp = if (item.dateTaken > 0) item.dateTaken else item.dateAdded
            val dayKey = timestamp / 86_400_000L

            val label = dayLabelCache.getOrPut(dayKey) {
                calendar.timeInMillis = timestamp
                when {
                    isSameDay(calendar, today) -> "Today"
                    isSameDay(calendar, yesterday) -> "Yesterday"
                    calendar.after(thisWeekStart) -> dayMonthFormat.format(Date(timestamp))
                    isSameYear(calendar, today) -> dayMonthFormat.format(Date(timestamp))
                    else -> monthYearFormat.format(Date(timestamp))
                }
            }

            groups.getOrPut(label) { ArrayList() }.add(item)
            if (!groupDates.containsKey(label)) {
                groupDates[label] = timestamp
            }
        }

        val result = groups.map { (label, groupItems) ->
            DateGroup(
                label = label,
                date = groupDates[label] ?: 0L,
                items = groupItems
            )
        }
        return if (descending) {
            result.sortedByDescending { it.date }
        } else {
            result.sortedBy { it.date }
        }
    }

    fun groupByName(items: List<MediaItem>, ascending: Boolean = true): List<DateGroup> {
        val groups = LinkedHashMap<String, MutableList<MediaItem>>()
        for (item in items) {
            val ch = item.displayName.firstOrNull()?.uppercaseChar()
            val label = if (ch != null && ch.isLetter()) ch.toString() else "#"
            groups.getOrPut(label) { ArrayList() }.add(item)
        }
        val result = groups.entries.mapIndexed { index, entry ->
            DateGroup(
                label = entry.key,
                date = index.toLong(),
                items = entry.value
            )
        }
        return if (ascending) {
            result.sortedBy { it.label }
        } else {
            result.sortedByDescending { it.label }
        }
    }

    fun groupBySize(items: List<MediaItem>, descending: Boolean = true): List<DateGroup> {
        val brackets = if (descending) {
            listOf(
                "> 100 MB" to { size: Long -> size >= 100 * 1024 * 1024L },
                "25 – 100 MB" to { size: Long -> size in (25 * 1024 * 1024L)..<(100 * 1024 * 1024L) },
                "5 – 25 MB" to { size: Long -> size in (5 * 1024 * 1024L)..<(25 * 1024 * 1024L) },
                "1 – 5 MB" to { size: Long -> size in (1 * 1024 * 1024L)..<(5 * 1024 * 1024L) },
                "< 1 MB" to { size: Long -> size < 1 * 1024 * 1024L }
            )
        } else {
            listOf(
                "< 1 MB" to { size: Long -> size < 1 * 1024 * 1024L },
                "1 – 5 MB" to { size: Long -> size in (1 * 1024 * 1024L)..<(5 * 1024 * 1024L) },
                "5 – 25 MB" to { size: Long -> size in (5 * 1024 * 1024L)..<(25 * 1024 * 1024L) },
                "25 – 100 MB" to { size: Long -> size in (25 * 1024 * 1024L)..<(100 * 1024 * 1024L) },
                "> 100 MB" to { size: Long -> size >= 100 * 1024 * 1024L }
            )
        }

        val groups = LinkedHashMap<String, MutableList<MediaItem>>()
        for (item in items) {
            val bracket = brackets.firstOrNull { it.second(item.size) }?.first ?: "Other"
            groups.getOrPut(bracket) { ArrayList() }.add(item)
        }

        return brackets.mapIndexedNotNull { index, (label, _) ->
            val groupItems = groups[label] ?: return@mapIndexedNotNull null
            DateGroup(
                label = label,
                date = index.toLong(),
                items = groupItems
            )
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}
