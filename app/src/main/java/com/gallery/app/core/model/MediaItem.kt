package com.gallery.app.core.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val dateTaken: Long,
    val duration: Long = 0,
    val bucketId: Long = 0,
    val bucketName: String = "",
    val relativePath: String = "",
    val orientation: Int = 0,
    val isFavorite: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val isVideo: Boolean get() = mimeType.startsWith("video/")
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isScreenshot: Boolean get() = bucketName.equals("Screenshots", ignoreCase = true) || relativePath.contains("Screenshots", ignoreCase = true)
    val isDownload: Boolean get() = bucketName.equals("Download", ignoreCase = true) || relativePath.contains("Download", ignoreCase = true)
    val isCamera: Boolean get() = bucketName.equals("Camera", ignoreCase = true) || bucketName.equals("DCIM", ignoreCase = true)
}
