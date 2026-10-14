package com.gallery.app.ui.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object GalleryColors {
    // Pure OLED Black & Solid Dark Surfaces (One UI visual target)
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF000000)
    val SurfaceLevel1 = Color(0xFF101012)
    val SurfaceContainer = Color(0xFF171719)
    val SurfaceContainerHigh = Color(0xFF202023)
    val SurfaceContainerHighest = Color(0xFF2C2C2E)
    
    // Samsung One UI Vibrant Accent Colors
    val Primary = Color(0xFF3875FF)
    val OnPrimary = Color(0xFFFFFFFF)
    val AccentBlue = Color(0xFF3875FF)
    
    // Text Hierarchy
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF8E8E93)
    val TextTertiary = Color(0xFF636366)
    
    // UI Functional Colors
    val SelectionBlue = Color(0xFF3875FF)
    val SelectionOverlay = Color(0x333875FF)
    val FavoriteRed = Color(0xFFFF453A)
    val DestructiveRed = Color(0xFFFF453A)
    val WarningOrange = Color(0xFFFF9F0A)
    val LockerBlue = Color(0xFF1E88E5)
    val LockerGold = Color(0xFFFFB300)
    val SuccessGreen = Color(0xFF34C759)
    
    // Outlines & Dividers
    val Divider = Color(0xFF1C1C1E)
    val Outline = Color(0xFF2C2C2E)
    val OutlineVariant = Color(0xFF1C1C1E)

    // Category Icon Accents
    val CategoryPeople = Color(0xFF5E5CE6)
    val CategoryPlaces = Color(0xFF30D158)
    val CategoryVideos = Color(0xFF64D2FF)
    val CategoryScreenshots = Color(0xFFFF9F0A)
    val CategoryFavorites = Color(0xFFFFD60A)
    val CategoryDocuments = Color(0xFFBF5AF2)
}

val LocalGalleryColors = staticCompositionLocalOf { GalleryColors }
