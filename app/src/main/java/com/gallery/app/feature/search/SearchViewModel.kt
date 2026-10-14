package com.gallery.app.feature.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gallery.app.core.database.GalleryDatabase
import com.gallery.app.core.media.MediaRepository
import com.gallery.app.core.model.MediaItem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(FlowPreview::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val database = GalleryDatabase.getInstance(application)
    
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    
    private val _results = MutableStateFlow<List<MediaItem>>(emptyList())
    val results: StateFlow<List<MediaItem>> = _results.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    
    init {
        viewModelScope.launch {
            _query.debounce(300).collect { q ->
                if (q.isNotBlank()) {
                    performSearch(q)
                } else {
                    _results.value = emptyList()
                }
            }
        }
    }
    
    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }
    
    fun clearQuery() {
        _query.value = ""
        _results.value = emptyList()
    }
    
    fun search() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            performSearch(_query.value)
        }
    }
    
    private suspend fun performSearch(query: String) {
        _isSearching.value = true
        try {
            val allMedia = mediaRepository.getAllMedia()
            val favIds = database.favoriteDao().getAllFavoriteIds().first().toSet()
            val q = query.lowercase().trim()
            
            val filtered = allMedia.filter { item ->
                when {
                    // Type filters
                    q == "screenshots" || q == "screenshot" -> item.isScreenshot
                    q == "videos" || q == "video" -> item.isVideo
                    q == "photos" || q == "photo" || q == "images" || q == "image" -> item.isImage
                    q == "favorites" || q == "favourite" || q == "favorited" -> item.id in favIds
                    q == "downloads" || q == "download" || q == "downloaded" -> item.isDownload
                    
                    // Month names
                    isMonthQuery(q) -> {
                        val monthNum = getMonthNumber(q)
                        if (monthNum >= 0) {
                            val cal = Calendar.getInstance()
                            val timestamp = item.dateTaken.takeIf { it > 0 } ?: item.dateAdded
                            cal.timeInMillis = timestamp
                            cal.get(Calendar.MONTH) == monthNum
                        } else false
                    }
                    
                    // Filename
                    else -> {
                        item.displayName.lowercase().contains(q) ||
                        item.bucketName.lowercase().contains(q) ||
                        item.mimeType.lowercase().contains(q) ||
                        matchesDate(item, q)
                    }
                }
            }
            _results.value = filtered
        } catch (e: Exception) {
            _results.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }
    
    private fun isMonthQuery(q: String): Boolean {
        val months = listOf("january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november", "december",
            "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
        return q in months
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
        return monthMap[q] ?: -1
    }
    
    private fun matchesDate(item: MediaItem, query: String): Boolean {
        val timestamp = item.dateTaken.takeIf { it > 0 } ?: item.dateAdded
        val date = Date(timestamp)
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("yyyy", Locale.getDefault()),
            SimpleDateFormat("MMMM", Locale.getDefault()),
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()),
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        )
        return formats.any { fmt ->
            fmt.format(date).lowercase().contains(query)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaRepository.cleanup()
    }
    
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(app) as T
            }
        }
    }
}
