package com.gallery.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recycle_bin")
data class RecycleBinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalMediaId: Long,
    val originalUri: String,
    val cachedFilePath: String,
    val mimeType: String,
    val displayName: String,
    val width: Int = 0,
    val height: Int = 0,
    val size: Long = 0,
    val originalBucketName: String? = null,
    val originalDateTaken: Long = 0,
    val deletedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
)
