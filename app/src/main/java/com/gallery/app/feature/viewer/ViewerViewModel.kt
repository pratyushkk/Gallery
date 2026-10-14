package com.gallery.app.feature.viewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gallery.app.core.database.GalleryDatabase
import com.gallery.app.core.database.entity.FavoriteEntity
import com.gallery.app.core.media.MediaRepository
import com.gallery.app.core.model.MediaItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ViewerUiState {
    data object Loading : ViewerUiState()
    data class Success(val currentItem: MediaItem, val allMedia: List<MediaItem>) : ViewerUiState()
    data class Error(val message: String) : ViewerUiState()
}

class ViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val database = GalleryDatabase.getInstance(application)
    private val favoriteDao = database.favoriteDao()
    
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()
    
    private val _currentItem = MutableStateFlow<MediaItem?>(null)
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    fun loadMedia(mediaId: Long) {
        viewModelScope.launch {
            try {
                val allMedia = mediaRepository.getAllMedia()
                val item = allMedia.find { it.id == mediaId }
                if (item != null) {
                    _currentItem.value = item
                    _uiState.value = ViewerUiState.Success(item, allMedia)
                    _isFavorite.value = favoriteDao.isFavoriteSync(item.id)
                } else {
                    _uiState.value = ViewerUiState.Error("Media not found")
                }
            } catch (e: Exception) {
                _uiState.value = ViewerUiState.Error(e.message ?: "Error loading media")
            }
        }
    }
    
    fun updateCurrentItem(item: MediaItem) {
        _currentItem.value = item
        viewModelScope.launch {
            _isFavorite.value = favoriteDao.isFavoriteSync(item.id)
        }
    }
    
    fun toggleFavorite() {
        val item = _currentItem.value ?: return
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoriteDao.removeFavorite(item.id)
                _isFavorite.value = false
            } else {
                favoriteDao.addFavorite(FavoriteEntity(mediaId = item.id))
                _isFavorite.value = true
            }
        }
    }
    
    private val _deleteIntentSender = kotlinx.coroutines.flow.MutableSharedFlow<android.content.IntentSender?>()
    val deleteIntentSender: kotlinx.coroutines.flow.SharedFlow<android.content.IntentSender?> = _deleteIntentSender

    fun deleteMedia(item: MediaItem) {
        viewModelScope.launch {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val intentSender = mediaRepository.createDeleteRequest(listOf(item))
                if (intentSender != null) {
                    _deleteIntentSender.emit(intentSender)
                } else {
                    mediaRepository.deleteMedia(item)
                }
            } else {
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
        }
    }

    private val lockerManager = com.gallery.app.core.security.LockerManager(getApplication())

    fun moveToLocker(item: MediaItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(item.uri) ?: return@launch
                val encryptedName = "enc_${System.currentTimeMillis()}_${item.id}.bin"
                lockerManager.encryptFile(inputStream, encryptedName)

                val lockerEntity = com.gallery.app.core.database.entity.LockerItemEntity(
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

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val intentSender = mediaRepository.createDeleteRequest(listOf(item))
                    if (intentSender != null) {
                        _deleteIntentSender.emit(intentSender)
                    } else {
                        mediaRepository.deleteMedia(item)
                    }
                } else {
                    try {
                        mediaRepository.deleteMedia(item)
                    } catch (_: Exception) {}
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                android.util.Log.e("ViewerViewModel", "Error moving to locker", e)
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
                return ViewerViewModel(app) as T
            }
        }
    }
}
