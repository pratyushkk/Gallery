package com.gallery.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gallery.app.core.model.MediaItem
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryShapes
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryThumbnail(
    item: MediaItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Remember the ImageRequest to avoid building new objects on every frame/recomposition
    val imageRequest = remember(item.uri) {
        ImageRequest.Builder(context)
            .data(item.uri)
            .memoryCacheKey("${item.uri}_thumb")
            .size(500) // Super sharp supersampled thumbnail for 1080p/1440p displays
            .precision(coil.size.Precision.INEXACT)
            .allowHardware(true)
            .allowRgb565(false)
            .crossfade(false) // Snappy instant display, removes animation jank during fling
            .build()
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(GalleryDimensions.photoGridSpacing / 2)
            .clip(GalleryShapes.thumbnail)
            .background(GalleryColors.SurfaceContainer)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Selection overlay
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GalleryColors.SelectionOverlay)
            )
        }

        // Selection Checkmark (matching One UI reference top-right circle)
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(20.dp)
                    .background(
                        color = if (isSelected) GalleryColors.SelectionBlue else Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) GalleryColors.SelectionBlue else Color.White.copy(alpha = 0.8f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Video duration badge
        if (item.isVideo && item.duration > 0) {
            val formattedDuration = remember(item.duration) { formatVideoDuration(item.duration) }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formattedDuration,
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }

        // Favorite indicator
        if (isFavorite && !isSelectionMode) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = GalleryColors.FavoriteRed,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(14.dp)
            )
        }
    }
}

private fun formatVideoDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
    }
}
