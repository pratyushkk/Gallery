package com.gallery.app.core.model

import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class MediaItemTest {

    private val mockUri = mock(Uri::class.java)

    @Test
    fun testIsImageAndIsVideo() {
        val imageItem = MediaItem(
            id = 1,
            uri = mockUri,
            displayName = "photo.jpg",
            mimeType = "image/jpeg",
            width = 1920,
            height = 1080,
            size = 1024,
            dateAdded = 1000,
            dateModified = 1000,
            dateTaken = 1000
        )
        assertTrue(imageItem.isImage)
        assertFalse(imageItem.isVideo)

        val videoItem = imageItem.copy(
            id = 2,
            displayName = "video.mp4",
            mimeType = "video/mp4",
            duration = 15000
        )
        assertFalse(videoItem.isImage)
        assertTrue(videoItem.isVideo)
    }

    @Test
    fun testScreenshotsAndDownloadsClassification() {
        val screenshot = MediaItem(
            id = 3,
            uri = mockUri,
            displayName = "Screenshot_2026.png",
            mimeType = "image/png",
            width = 1080,
            height = 2400,
            size = 2048,
            dateAdded = 1000,
            dateModified = 1000,
            dateTaken = 1000,
            bucketName = "Screenshots"
        )
        assertTrue(screenshot.isScreenshot)
        assertFalse(screenshot.isDownload)

        val download = MediaItem(
            id = 4,
            uri = mockUri,
            displayName = "downloaded_file.jpg",
            mimeType = "image/jpeg",
            width = 800,
            height = 600,
            size = 512,
            dateAdded = 1000,
            dateModified = 1000,
            dateTaken = 1000,
            bucketName = "Download"
        )
        assertFalse(download.isScreenshot)
        assertTrue(download.isDownload)
    }

    @Test
    fun testCameraClassification() {
        val cameraPic = MediaItem(
            id = 5,
            uri = mockUri,
            displayName = "IMG_20261006.jpg",
            mimeType = "image/jpeg",
            width = 4000,
            height = 3000,
            size = 5000000,
            dateAdded = 1000,
            dateModified = 1000,
            dateTaken = 1000,
            bucketName = "Camera"
        )
        assertTrue(cameraPic.isCamera)
    }
}
