package com.gallery.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    if (scrollBehavior != null) {
        LargeTopAppBar(
            title = {
                Text(
                    text = title,
                    style = GalleryTypography.screenTitle,
                    color = GalleryColors.TextPrimary
                )
            },
            navigationIcon = {
                if (navigationIcon != null) {
                    navigationIcon()
                }
            },
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = GalleryColors.Background,
                scrolledContainerColor = GalleryColors.SurfaceLevel1,
                titleContentColor = GalleryColors.TextPrimary,
                actionIconContentColor = GalleryColors.TextPrimary,
                navigationIconContentColor = GalleryColors.TextPrimary
            ),
            modifier = modifier
        )
    } else {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = GalleryTypography.screenTitle,
                    color = GalleryColors.TextPrimary
                )
            },
            navigationIcon = {
                if (navigationIcon != null) {
                    navigationIcon()
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = GalleryColors.Background,
                titleContentColor = GalleryColors.TextPrimary,
                actionIconContentColor = GalleryColors.TextPrimary,
                navigationIconContentColor = GalleryColors.TextPrimary
            ),
            modifier = modifier
        )
    }
}
