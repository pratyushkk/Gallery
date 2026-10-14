package com.gallery.app.feature.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gallery.app.core.media.MediaRepository
import com.gallery.app.core.database.GalleryDatabase
import com.gallery.app.core.model.MediaItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AlbumDetailUiState {
    data object Loading : AlbumDetailUiState()
    data object Empty : AlbumDetailUiState()
    data class Success(
        val albumName: String,
        val media: List<MediaItem>
    ) : AlbumDetailUiState()
}

class AlbumDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val database = GalleryDatabase.getInstance(application)
    
    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            try {
                val allMedia = mediaRepository.getAllMedia()
                val (name, items) = when {
                    albumId == "camera" -> "Camera" to allMedia.filter { it.isCamera }
                    albumId == "screenshots" -> "Screenshots" to allMedia.filter { it.isScreenshot }
                    albumId == "downloads" -> "Downloads" to allMedia.filter { it.isDownload }
                    albumId == "videos" -> "Videos" to allMedia.filter { it.isVideo }
                    albumId == "favorites" -> {
                        val favIds = database.favoriteDao().getAllFavoriteIds().first().toSet()
                        "Favorites" to allMedia.filter { it.id in favIds }
                    }
                    albumId.startsWith("bucket_") -> {
                        val bucketId = albumId.removePrefix("bucket_").toLongOrNull() ?: 0
                        val bucketItems = allMedia.filter { it.bucketId == bucketId }
                        (bucketItems.firstOrNull()?.bucketName ?: "Album") to bucketItems
                    }
                    albumId.startsWith("user_") -> {
                        val dbId = albumId.removePrefix("user_").toLongOrNull() ?: 0
                        val album = database.albumDao().getAlbumById(dbId)
                        val mediaIds = database.albumDao().getMediaIdsForAlbum(dbId).first()
                        (album?.name ?: "Album") to allMedia.filter { it.id in mediaIds }
                    }
                    else -> albumId to emptyList()
                }
                
                if (items.isEmpty()) {
                    _uiState.value = AlbumDetailUiState.Empty
                } else {
                    _uiState.value = AlbumDetailUiState.Success(
                        albumName = name,
                        media = items
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AlbumDetailUiState.Empty
            }
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
                return AlbumDetailViewModel(app) as T
            }
        }
    }
}
