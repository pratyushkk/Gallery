package com.gallery.app.feature.photos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gallery.app.core.database.GalleryDatabase
import com.gallery.app.core.database.entity.FavoriteEntity
import com.gallery.app.core.database.entity.LockerItemEntity
import com.gallery.app.core.media.DateGrouper
import com.gallery.app.core.media.MediaRepository
import com.gallery.app.core.model.DateGroup
import com.gallery.app.core.model.MediaItem
import com.gallery.app.core.model.SortOrder
import com.gallery.app.core.security.LockerManager
import com.gallery.app.core.settings.AppSettings
import com.gallery.app.core.settings.GridDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class PhotosUiState {
    data object Loading : PhotosUiState()
    data object Empty : PhotosUiState()
    data class Success(
        val dateGroups: List<DateGroup>,
        val favoriteIds: Set<Long>
    ) : PhotosUiState()
    data class Error(val message: String) : PhotosUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val database = GalleryDatabase.getInstance(application)
    private val favoriteDao = database.favoriteDao()
    private val lockerManager = LockerManager(application)
    private val appSettings = AppSettings(application)
    
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _gridDensity = MutableStateFlow(GridDensity.NORMAL)
    val gridDensity: StateFlow<GridDensity> = _gridDensity.asStateFlow()
    
    private var allMedia: List<MediaItem> = emptyList()

    init {
        viewModelScope.launch {
            appSettings.sortOrderFlow.collect { order ->
                _sortOrder.value = order
            }
        }
        viewModelScope.launch {
            appSettings.gridDensity.collect { density ->
                _gridDensity.value = density
            }
        }
    }
    
    val uiState: StateFlow<PhotosUiState> = combine(
        _sortOrder.flatMapLatest { order -> mediaRepository.observeAllMedia(order) },
        favoriteDao.getAllFavoriteIds(),
        _sortOrder
    ) { media, favIds, currentSort ->
        allMedia = media
        if (media.isEmpty()) {
            PhotosUiState.Empty
        } else {
            val groups = DateGrouper.groupBySortOrder(media, currentSort)
            PhotosUiState.Success(
                dateGroups = groups,
                favoriteIds = favIds.toSet()
            )
        }
    }.flowOn(Dispatchers.Default)
    .catch { e ->
        emit(PhotosUiState.Error(e.message ?: "Failed to load media"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PhotosUiState.Loading
    )

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        viewModelScope.launch {
            appSettings.setSetting(AppSettings.SORT_ORDER, order.name)
        }
    }

    fun setGridDensity(density: GridDensity) {
        _gridDensity.value = density
        viewModelScope.launch {
            appSettings.setSetting(AppSettings.GRID_DENSITY, density.name)
        }
    }

    fun zoomGrid(zoomIn: Boolean) {
        val current = _gridDensity.value
        val next = if (zoomIn) {
            when (current) {
                GridDensity.COMPACT -> GridDensity.NORMAL
                GridDensity.NORMAL -> GridDensity.COMFORTABLE
                GridDensity.COMFORTABLE -> GridDensity.COMFORTABLE
            }
        } else {
            when (current) {
                GridDensity.COMFORTABLE -> GridDensity.NORMAL
                GridDensity.NORMAL -> GridDensity.COMPACT
                GridDensity.COMPACT -> GridDensity.COMPACT
            }
        }
        if (next != current) {
            setGridDensity(next)
        }
    }
    
    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }
    
    fun clearSelection() {
        _selectedIds.value = emptySet()
        _isSelectionMode.value = false
    }
    
    fun toggleSelection(id: Long) {
        val current = _selectedIds.value
        val newSet = if (id in current) current - id else current + id
        _selectedIds.value = newSet
        if (newSet.isEmpty()) {
            _isSelectionMode.value = false
        }
    }
    
    fun selectAll() {
        _selectedIds.value = allMedia.map { it.id }.toSet()
    }
    
    fun toggleGroupSelection(group: DateGroup) {
        val current = _selectedIds.value
        val groupIds = group.items.map { it.id }.toSet()
        val newSet = if (groupIds.all { it in current }) current - groupIds else current + groupIds
        _selectedIds.value = newSet
    }
    
    fun toggleFavoriteSelected() {
        viewModelScope.launch {
            for (id in _selectedIds.value) {
                val isFav = favoriteDao.isFavoriteSync(id)
                if (isFav) {
                    favoriteDao.removeFavorite(id)
                } else {
                    favoriteDao.addFavorite(FavoriteEntity(mediaId = id))
                }
            }
            clearSelection()
        }
    }
    
    private val _deleteIntentSender = MutableSharedFlow<android.content.IntentSender?>()
    val deleteIntentSender: SharedFlow<android.content.IntentSender?> = _deleteIntentSender

    fun deleteSelected() {
        viewModelScope.launch {
            val itemsToDelete = _selectedIds.value.mapNotNull { id -> allMedia.find { it.id == id } }
            if (itemsToDelete.isEmpty()) return@launch

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val intentSender = mediaRepository.createDeleteRequest(itemsToDelete)
                if (intentSender != null) {
                    _deleteIntentSender.emit(intentSender)
                } else {
                    itemsToDelete.forEach { mediaRepository.deleteMedia(it) }
                    clearSelection()
                }
            } else {
                itemsToDelete.forEach { item ->
                    try {
                        mediaRepository.deleteMedia(item)
                    } catch (e: SecurityException) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            val recoverableSecurityException = e as? android.app.RecoverableSecurityException
                            recoverableSecurityException?.userAction?.actionIntent?.intentSender?.let {
                                _deleteIntentSender.emit(it)
                            }
                        }
                    }
                }
                clearSelection()
            }
        }
    }

    fun moveSelectedToLocker(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val itemsToLock = _selectedIds.value.mapNotNull { id -> allMedia.find { it.id == id } }
            if (itemsToLock.isEmpty()) return@launch

            var count = 0
            val context = getApplication<Application>()
            for (item in itemsToLock) {
                try {
                    val inputStream = context.contentResolver.openInputStream(item.uri) ?: continue
                    val encryptedName = "enc_${System.currentTimeMillis()}_${item.id}.bin"
                    lockerManager.encryptFile(inputStream, encryptedName)
                    
                    val lockerEntity = LockerItemEntity(
                        originalMediaId = item.id,
                        originalUri = item.uri.toString(),
                        encryptedFileName = encryptedName,
                        mimeType = item.mimeType,
                        width = item.width,
                        height = item.height,
                        size = item.size,
                        dateAdded = System.currentTimeMillis(),
                        originalDateTaken = item.dateTaken,
                        displayName = item.displayName
                    )
                    database.lockerDao().insertLockerItem(lockerEntity)
                    count++
                } catch (e: Exception) {
                    android.util.Log.e("PhotosViewModel", "Error encrypting to locker", e)
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val intentSender = mediaRepository.createDeleteRequest(itemsToLock)
                if (intentSender != null) {
                    _deleteIntentSender.emit(intentSender)
                } else {
                    itemsToLock.forEach { mediaRepository.deleteMedia(it) }
                    clearSelection()
                }
            } else {
                itemsToLock.forEach { item ->
                    try {
                        mediaRepository.deleteMedia(item)
                    } catch (_: Exception) {}
                }
                clearSelection()
            }

            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaRepository.cleanup()
    }
    
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                throw IllegalStateException("Use the other create method")
            }
            
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                @Suppress("UNCHECKED_CAST")
                return PhotosViewModel(app) as T
            }
        }
    }
}
