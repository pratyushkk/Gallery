package com.gallery.app.feature.recyclebin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gallery.app.core.database.GalleryDatabase
import com.gallery.app.core.database.entity.RecycleBinEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class RecycleBinViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GalleryDatabase.getInstance(application)
    private val recycleBinDao = database.recycleBinDao()
    
    val items: StateFlow<List<RecycleBinEntity>> = recycleBinDao.getAllRecycleBinItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // Clean up expired items on launch
        viewModelScope.launch {
            recycleBinDao.deleteExpiredItems()
        }
    }
    
    fun restoreItem(item: RecycleBinEntity) {
        viewModelScope.launch {
            // In a real implementation, we'd re-insert into MediaStore
            // For now, remove from recycle bin
            recycleBinDao.deleteRecycleBinItem(item)
        }
    }
    
    fun permanentlyDelete(item: RecycleBinEntity) {
        viewModelScope.launch {
            val file = File(item.cachedFilePath)
            file.delete()
            recycleBinDao.deleteRecycleBinItem(item)
        }
    }
    
    fun emptyBin() {
        viewModelScope.launch {
            items.value.forEach { item ->
                val file = File(item.cachedFilePath)
                file.delete()
            }
            recycleBinDao.emptyRecycleBin()
        }
    }
    
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                @Suppress("UNCHECKED_CAST")
                return RecycleBinViewModel(app) as T
            }
        }
    }
}
