package com.gallery.app.ui.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GalleryDimensions {
    // Spacing scale
    val spaceXxs: Dp = 2.dp
    val spaceXs: Dp = 4.dp
    val spaceSm: Dp = 8.dp
    val spaceMd: Dp = 12.dp
    val spaceNormal: Dp = 16.dp
    val spaceLg: Dp = 20.dp
    val spaceXl: Dp = 24.dp
    val spaceXxl: Dp = 32.dp

    // Screen Margins & Headers
    val screenHorizontalPadding: Dp = 16.dp
    val largeTitleTopPadding: Dp = 14.dp
    val largeTitleBottomPadding: Dp = 10.dp
    val topBarHeight: Dp = 56.dp
    val bottomNavHeight: Dp = 62.dp

    // Grids
    val photoGridColumns: Int = 4
    val photoGridSpacing: Dp = 2.dp
    val photoThumbnailCorner: Dp = 2.dp

    val albumGridColumns: Int = 3
    val albumGridSpacing: Dp = 12.dp
    val albumCardRadius: Dp = 16.dp

    // Search
    val searchBarHeight: Dp = 48.dp
    val searchBarRadius: Dp = 24.dp
    val searchCategoryChipHeight: Dp = 40.dp
    val searchCategoryChipRadius: Dp = 12.dp

    // Bottom Sheets & Dialogs
    val sheetCornerRadius: Dp = 28.dp
    val cardCornerRadius: Dp = 16.dp
    val buttonCornerRadius: Dp = 24.dp
    val pillCornerRadius: Dp = 20.dp

    // Icon sizes
    val iconXs: Dp = 16.dp
    val iconSm: Dp = 20.dp
    val iconNormal: Dp = 24.dp
    val iconLg: Dp = 28.dp
    val iconXl: Dp = 48.dp
    val iconAuth: Dp = 64.dp
    val fingerprintCircleSize: Dp = 76.dp
}

val LocalGalleryDimensions = staticCompositionLocalOf { GalleryDimensions }
