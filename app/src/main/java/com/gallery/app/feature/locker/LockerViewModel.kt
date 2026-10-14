package com.gallery.app.feature.locker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gallery.app.core.database.GalleryDatabase
import com.gallery.app.core.database.entity.LockerItemEntity
import com.gallery.app.core.security.LockerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LockerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GalleryDatabase.getInstance(application)
    private val lockerDao = database.lockerDao()
    private val lockerManager = LockerManager(application)
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()
    
    val lockerItems: StateFlow<List<LockerItemEntity>> = lockerDao.getAllLockerItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun authenticate() {
        _isAuthenticated.value = true
    }
    
    fun lock() {
        _isAuthenticated.value = false
        clearSelection()
        lockerManager.cleanupTempFiles()
    }

    fun getDecryptedFile(item: LockerItemEntity): File {
        return lockerManager.getDecryptedTempFile(item.encryptedFileName)
    }

    fun restoreLockerItem(item: LockerItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val restoredUri = lockerManager.restoreToGallery(item)
            if (restoredUri != null) {
                lockerDao.deleteLockerItem(item)
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
    
    fun deleteLockerItem(item: LockerItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            lockerManager.deleteEncryptedFile(item.encryptedFileName)
            item.encryptedThumbnailName?.let { lockerManager.deleteEncryptedFile(it) }
            lockerDao.deleteLockerItem(item)
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
        _selectedIds.value = lockerItems.value.map { it.id }.toSet()
    }

    fun restoreSelected(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = lockerItems.value.filter { it.id in _selectedIds.value }
            var count = 0
            items.forEach { item ->
                val uri = lockerManager.restoreToGallery(item)
                if (uri != null) {
                    lockerDao.deleteLockerItem(item)
                    count++
                }
            }
            clearSelection()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = lockerItems.value.filter { it.id in _selectedIds.value }
            items.forEach { item ->
                lockerManager.deleteEncryptedFile(item.encryptedFileName)
                item.encryptedThumbnailName?.let { lockerManager.deleteEncryptedFile(it) }
                lockerDao.deleteLockerItem(item)
            }
            clearSelection()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        lockerManager.cleanupTempFiles()
    }
    
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                @Suppress("UNCHECKED_CAST")
                return LockerViewModel(app) as T
            }
        }
    }
}
