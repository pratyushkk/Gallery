package com.gallery.app.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

object GalleryShapes {
    val thumbnail = RoundedCornerShape(2.dp)
    val albumCard = RoundedCornerShape(16.dp)
    val categoryChip = RoundedCornerShape(12.dp)
    val searchBar = RoundedCornerShape(24.dp)
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val dialog = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
    val card = RoundedCornerShape(16.dp)
    val squircleIcon = RoundedCornerShape(12.dp)
}

val LocalGalleryShapes = staticCompositionLocalOf { GalleryShapes }
