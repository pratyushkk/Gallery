package com.gallery.app.core.database.entity

import androidx.room.Entity

@Entity(
    tableName = "album_media",
    primaryKeys = ["albumId", "mediaId"]
)
data class AlbumMediaCrossRef(
    val albumId: Long,
    val mediaId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
