package com.gallery.app.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class DateGroup(
    val label: String,
    val date: Long,
    val items: List<MediaItem>
)
