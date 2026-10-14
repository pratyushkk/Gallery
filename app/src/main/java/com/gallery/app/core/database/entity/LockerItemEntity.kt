package com.gallery.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locker_items")
data class LockerItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalMediaId: Long,
    val originalUri: String,
    val encryptedFileName: String,
    val mimeType: String,
    val width: Int = 0,
    val height: Int = 0,
    val size: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val originalDateTaken: Long = 0,
    val displayName: String = "",
    val encryptedThumbnailName: String? = null
)
