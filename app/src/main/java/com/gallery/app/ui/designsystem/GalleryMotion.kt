package com.gallery.app.ui.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

object GalleryMotion {
    const val durationFast = 150
    const val durationNormal = 250
    const val durationSlow = 350

    val oneUiEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    fun <T> standardTween(duration: Int = durationNormal) = tween<T>(
        durationMillis = duration,
        easing = oneUiEasing
    )

    val enterSlideUp = slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = standardTween()
    ) + fadeIn(animationSpec = standardTween())

    val exitSlideDown = slideOutVertically(
        targetOffsetY = { it / 2 },
        animationSpec = standardTween()
    ) + fadeOut(animationSpec = standardTween())
}
