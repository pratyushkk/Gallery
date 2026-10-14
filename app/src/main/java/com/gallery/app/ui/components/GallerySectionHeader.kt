package com.gallery.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryTypography

@Composable
fun GallerySectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = GalleryDimensions.screenHorizontalPadding,
                vertical = GalleryDimensions.spaceSm
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = GalleryTypography.sectionHeader,
            color = GalleryColors.TextPrimary
        )

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = GalleryTypography.bodyMedium,
                color = GalleryColors.AccentBlue,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}
