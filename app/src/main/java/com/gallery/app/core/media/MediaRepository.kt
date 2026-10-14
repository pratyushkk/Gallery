package com.gallery.app.core.media

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.gallery.app.core.model.MediaItem
import com.gallery.app.core.model.Album
import com.gallery.app.core.model.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver
    
    private val _mediaChanges = MutableSharedFlow<Unit>(replay = 1)
    val mediaChanges: SharedFlow<Unit> = _mediaChanges

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            _mediaChanges.tryEmit(Unit)
        }
    }

    init {
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, contentObserver
        )
    }

    private val imageProjection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.RELATIVE_PATH,
        MediaStore.Images.Media.ORIENTATION
    )

    private val videoProjection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.DATE_TAKEN,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.BUCKET_ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.RELATIVE_PATH
    )

    suspend fun getAllMedia(sortOrder: SortOrder = SortOrder.DATE_DESC): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        items.addAll(queryImages(sortOrder))
        items.addAll(queryVideos(sortOrder))
        
        when (sortOrder) {
            SortOrder.DATE_DESC -> items.sortByDescending { it.dateTaken.takeIf { t -> t > 0 } ?: it.dateAdded }
            SortOrder.DATE_ASC -> items.sortBy { it.dateTaken.takeIf { t -> t > 0 } ?: it.dateAdded }
            SortOrder.NAME_ASC -> items.sortBy { it.displayName.lowercase() }
            SortOrder.NAME_DESC -> items.sortByDescending { it.displayName.lowercase() }
            SortOrder.SIZE_DESC -> items.sortByDescending { it.size }
            SortOrder.SIZE_ASC -> items.sortBy { it.size }
        }
        items
    }

    fun observeAllMedia(sortOrder: SortOrder = SortOrder.DATE_DESC): Flow<List<MediaItem>> = flow {
        emit(getAllMedia(sortOrder))
        mediaChanges.debounce(300).collect {
            emit(getAllMedia(sortOrder))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun queryImages(sortOrder: SortOrder): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        val sort = getSortString(sortOrder, isVideo = false)
        
        try {
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                sort
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val relPathCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
                val orientCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    items.add(
                        MediaItem(
                            id = id,
                            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                            displayName = cursor.getString(nameCol) ?: "Unknown",
                            mimeType = cursor.getString(mimeCol) ?: "image/*",
                            width = cursor.getInt(widthCol),
                            height = cursor.getInt(heightCol),
                            size = cursor.getLong(sizeCol),
                            dateAdded = cursor.getLong(dateAddedCol) * 1000,
                            dateModified = cursor.getLong(dateModCol) * 1000,
                            dateTaken = cursor.getLong(dateTakenCol),
                            bucketId = cursor.getLong(bucketIdCol),
                            bucketName = cursor.getString(bucketNameCol) ?: "",
                            relativePath = cursor.getString(relPathCol) ?: "",
                            orientation = cursor.getInt(orientCol)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaRepository", "Error querying images", e)
        }
        items
    }

    private suspend fun queryVideos(sortOrder: SortOrder): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        val sort = getSortString(sortOrder, isVideo = true)
        
        try {
            contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                sort
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val relPathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    items.add(
                        MediaItem(
                            id = id,
                            uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                            displayName = cursor.getString(nameCol) ?: "Unknown",
                            mimeType = cursor.getString(mimeCol) ?: "video/*",
                            width = cursor.getInt(widthCol),
                            height = cursor.getInt(heightCol),
                            size = cursor.getLong(sizeCol),
                            dateAdded = cursor.getLong(dateAddedCol) * 1000,
                            dateModified = cursor.getLong(dateModCol) * 1000,
                            dateTaken = cursor.getLong(dateTakenCol),
                            duration = cursor.getLong(durationCol),
                            bucketId = cursor.getLong(bucketIdCol),
                            bucketName = cursor.getString(bucketNameCol) ?: "",
                            relativePath = cursor.getString(relPathCol) ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaRepository", "Error querying videos", e)
        }
        items
    }

    suspend fun getMediaById(id: Long): MediaItem? = withContext(Dispatchers.IO) {
        val allMedia = getAllMedia()
        allMedia.find { it.id == id }
    }

    suspend fun getSystemAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val allMedia = getAllMedia()
        val bucketMap = allMedia.groupBy { it.bucketId }
        
        bucketMap.map { (bucketId, items) ->
            val firstItem = items.first()
            Album(
                id = "bucket_$bucketId",
                name = firstItem.bucketName.ifEmpty { "Unknown" },
                coverUri = items.maxByOrNull { it.dateTaken.takeIf { t -> t > 0 } ?: it.dateAdded }?.uri,
                mediaCount = items.size,
                isSystem = true
            )
        }.sortedByDescending { it.mediaCount }
    }

    suspend fun getMediaForBucket(bucketId: Long): List<MediaItem> = withContext(Dispatchers.IO) {
        getAllMedia().filter { it.bucketId == bucketId }
    }

    suspend fun deleteMedia(mediaItem: MediaItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val rows = contentResolver.delete(mediaItem.uri, null, null)
            rows > 0
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("MediaRepository", "Error deleting media", e)
            false
        }
    }
    
    fun createDeleteRequest(items: List<MediaItem>): android.content.IntentSender? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = items.map { it.uri }
            return MediaStore.createDeleteRequest(contentResolver, uris).intentSender
        }
        return null
    }

    private fun getSortString(sortOrder: SortOrder, isVideo: Boolean): String {
        val dateCol = if (isVideo) MediaStore.Video.Media.DATE_TAKEN else MediaStore.Images.Media.DATE_TAKEN
        val nameCol = if (isVideo) MediaStore.Video.Media.DISPLAY_NAME else MediaStore.Images.Media.DISPLAY_NAME
        val sizeCol = if (isVideo) MediaStore.Video.Media.SIZE else MediaStore.Images.Media.SIZE
        return when (sortOrder) {
            SortOrder.DATE_DESC -> "$dateCol DESC"
            SortOrder.DATE_ASC -> "$dateCol ASC"
            SortOrder.NAME_ASC -> "$nameCol ASC"
            SortOrder.NAME_DESC -> "$nameCol DESC"
            SortOrder.SIZE_DESC -> "$sizeCol DESC"
            SortOrder.SIZE_ASC -> "$sizeCol ASC"
        }
    }

    fun cleanup() {
        contentResolver.unregisterContentObserver(contentObserver)
    }
}
