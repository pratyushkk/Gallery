package com.gallery.app.feature.albums

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gallery.app.core.database.GalleryDatabase
import com.gallery.app.core.database.entity.AlbumEntity
import com.gallery.app.core.media.MediaRepository
import com.gallery.app.core.model.Album
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AlbumsUiState {
    data object Loading : AlbumsUiState()
    data class Success(
        val systemAlbums: List<Album>,
        val userAlbums: List<Album>,
        val lockerCount: Int,
        val recycleBinCount: Int
    ) : AlbumsUiState()
}

enum class AlbumSortOrder { DEFAULT, NAME, COUNT }

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val database = GalleryDatabase.getInstance(application)
    private val albumDao = database.albumDao()
    private val favoriteDao = database.favoriteDao()
    private val lockerDao = database.lockerDao()
    private val recycleBinDao = database.recycleBinDao()

    private val _sortOrder = MutableStateFlow(AlbumSortOrder.DEFAULT)
    val sortOrder: StateFlow<AlbumSortOrder> = _sortOrder.asStateFlow()

    fun setSortOrder(order: AlbumSortOrder) {
        _sortOrder.value = order
    }

    val uiState: StateFlow<AlbumsUiState> = combine(
        combine(
            flow { emit(mediaRepository.getAllMedia()) },
            albumDao.getAllAlbums(),
            favoriteDao.getFavoriteCount()
        ) { allMedia, userAlbumEntities, favCount ->
            Triple(allMedia, userAlbumEntities, favCount)
        },
        combine(
            lockerDao.getLockerItemCount(),
            recycleBinDao.getRecycleBinCount(),
            _sortOrder
        ) { lockerCount, binCount, sort ->
            Triple(lockerCount, binCount, sort)
        }
    ) { (allMedia, userAlbumEntities, favCount), (lockerCount, binCount, sort) ->
        val bucketGroups = allMedia.groupBy { it.bucketId }

        val systemAlbums = mutableListOf<Album>()

        // Camera
        val cameraItems = allMedia.filter { it.isCamera }
        if (cameraItems.isNotEmpty()) {
            systemAlbums.add(
                Album(
                    id = "camera",
                    name = "Camera",
                    coverUri = cameraItems.firstOrNull()?.uri,
                    mediaCount = cameraItems.size,
                    isSystem = true
                )
            )
        }

        // Screenshots
        val screenshots = allMedia.filter { it.isScreenshot }
        if (screenshots.isNotEmpty()) {
            systemAlbums.add(
                Album(
                    id = "screenshots",
                    name = "Screenshots",
                    coverUri = screenshots.firstOrNull()?.uri,
                    mediaCount = screenshots.size,
                    isSystem = true
                )
            )
        }

        // Downloads
        val downloads = allMedia.filter { it.isDownload }
        if (downloads.isNotEmpty()) {
            systemAlbums.add(
                Album(
                    id = "downloads",
                    name = "Downloads",
                    coverUri = downloads.firstOrNull()?.uri,
                    mediaCount = downloads.size,
                    isSystem = true
                )
            )
        }

        // Favorites
        if (favCount > 0) {
            systemAlbums.add(
                Album(
                    id = "favorites",
                    name = "Favorites",
                    coverUri = null,
                    mediaCount = favCount,
                    isSystem = true
                )
            )
        }

        // Videos
        val videos = allMedia.filter { it.isVideo }
        if (videos.isNotEmpty()) {
            systemAlbums.add(
                Album(
                    id = "videos",
                    name = "Videos",
                    coverUri = videos.firstOrNull()?.uri,
                    mediaCount = videos.size,
                    isSystem = true
                )
            )
        }

        // Other bucket albums not already shown
        val shownBuckets = mutableSetOf<Long>()
        for (item in cameraItems + screenshots + downloads) {
            shownBuckets.add(item.bucketId)
        }
        bucketGroups.filter { it.key !in shownBuckets }.forEach { (bucketId, items) ->
            systemAlbums.add(
                Album(
                    id = "bucket_$bucketId",
                    name = items.first().bucketName.ifEmpty { "Unknown" },
                    coverUri = items.firstOrNull()?.uri,
                    mediaCount = items.size,
                    isSystem = true
                )
            )
        }

        val userAlbums = userAlbumEntities.map { entity ->
            Album(
                id = "user_${entity.id}",
                name = entity.name,
                coverUri = entity.coverMediaId?.let {
                    allMedia.find { m -> m.id == it }?.uri
                },
                mediaCount = 0,
                isUserCreated = true,
                dbId = entity.id
            )
        }

        val sortedSystemAlbums = when (sort) {
            AlbumSortOrder.DEFAULT -> systemAlbums
            AlbumSortOrder.NAME -> systemAlbums.sortedBy { it.name.lowercase() }
            AlbumSortOrder.COUNT -> systemAlbums.sortedByDescending { it.mediaCount }
        }

        val sortedUserAlbums = when (sort) {
            AlbumSortOrder.DEFAULT -> userAlbums
            AlbumSortOrder.NAME -> userAlbums.sortedBy { it.name.lowercase() }
            AlbumSortOrder.COUNT -> userAlbums.sortedByDescending { it.mediaCount }
        }

        AlbumsUiState.Success(
            systemAlbums = sortedSystemAlbums,
            userAlbums = sortedUserAlbums,
            lockerCount = lockerCount,
            recycleBinCount = binCount
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumsUiState.Loading
    )

    fun createAlbum(name: String) {
        viewModelScope.launch {
            albumDao.insertAlbum(AlbumEntity(name = name))
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
                return AlbumsViewModel(app) as T
            }
        }
    }
}
