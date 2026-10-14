package com.gallery.app.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryShapes
import com.gallery.app.ui.designsystem.GalleryTypography
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GalleryAlbumCard(
    title: String,
    count: Int,
    coverUri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formattedCount = rememberFormattedCount(count)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(GalleryShapes.albumCard)
                .background(GalleryColors.SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            if (coverUri != null) {
                val imageRequest = remember(coverUri) {
                    ImageRequest.Builder(context)
                        .data(coverUri)
                        .memoryCacheKey("${coverUri}_thumb")
                        .size(500)
                        .precision(coil.size.Precision.INEXACT)
                        .allowHardware(true)
                        .allowRgb565(false)
                        .crossfade(false)
                        .build()
                }
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Photo,
                    contentDescription = null,
                    tint = GalleryColors.TextTertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = title,
            style = GalleryTypography.cardTitle,
            color = GalleryColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Count
        Text(
            text = formattedCount,
            style = GalleryTypography.cardSubtitle,
            color = GalleryColors.TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun rememberFormattedCount(count: Int): String {
    return remember(count) {
        NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
    }
}
