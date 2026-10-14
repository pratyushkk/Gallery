package com.gallery.app.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val OneUiDarkColorScheme = darkColorScheme(
    primary = GalleryColors.Primary,
    onPrimary = GalleryColors.OnPrimary,
    primaryContainer = GalleryColors.SurfaceContainerHigh,
    onPrimaryContainer = GalleryColors.OnSurface,
    secondary = GalleryColors.TextSecondary,
    onSecondary = GalleryColors.OnBackground,
    secondaryContainer = GalleryColors.SurfaceContainer,
    onSecondaryContainer = GalleryColors.OnSurface,
    background = GalleryColors.Background,
    onBackground = GalleryColors.OnBackground,
    surface = GalleryColors.Surface,
    onSurface = GalleryColors.OnSurface,
    surfaceVariant = GalleryColors.SurfaceContainer,
    onSurfaceVariant = GalleryColors.TextSecondary,
    outline = GalleryColors.Outline,
    outlineVariant = GalleryColors.OutlineVariant,
    surfaceContainerLowest = GalleryColors.Background,
    surfaceContainerLow = GalleryColors.SurfaceLevel1,
    surfaceContainer = GalleryColors.SurfaceContainer,
    surfaceContainerHigh = GalleryColors.SurfaceContainerHigh,
    surfaceContainerHighest = GalleryColors.SurfaceContainerHighest,
    error = GalleryColors.DestructiveRed,
    onError = GalleryColors.OnPrimary
)

@Composable
fun GalleryTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalGalleryColors provides GalleryColors,
        LocalGalleryDimensions provides GalleryDimensions,
        LocalGalleryTypography provides GalleryTypography,
        LocalGalleryShapes provides GalleryShapes
    ) {
        MaterialTheme(
            colorScheme = OneUiDarkColorScheme,
            content = content
        )
    }
}
