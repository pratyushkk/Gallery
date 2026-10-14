package com.gallery.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

class GalleryApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.35)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache_v2"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .components {
                add(coil.decode.VideoFrameDecoder.Factory())
            }
            .allowHardware(true)
            .allowRgb565(false)
            .crossfade(false)
            .respectCacheHeaders(false)
            .build()
    }
}
