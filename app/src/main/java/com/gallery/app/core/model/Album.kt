package com.gallery.app.core.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class Album(
    val id: String,
    val name: String,
    val coverUri: Uri? = null,
    val mediaCount: Int = 0,
    val isUserCreated: Boolean = false,
    val isSystem: Boolean = false,
    val dbId: Long? = null
)
