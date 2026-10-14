package com.gallery.app.core.database.dao

import androidx.room.*
import com.gallery.app.core.database.entity.AlbumEntity
import com.gallery.app.core.database.entity.AlbumMediaCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY updatedAt DESC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: Long): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMediaToAlbum(crossRef: AlbumMediaCrossRef)

    @Delete
    suspend fun removeMediaFromAlbum(crossRef: AlbumMediaCrossRef)

    @Query("SELECT mediaId FROM album_media WHERE albumId = :albumId ORDER BY addedAt DESC")
    fun getMediaIdsForAlbum(albumId: Long): Flow<List<Long>>

    @Query("SELECT albumId FROM album_media WHERE mediaId = :mediaId")
    suspend fun getAlbumsForMedia(mediaId: Long): List<Long>

    @Query("DELETE FROM album_media WHERE albumId = :albumId")
    suspend fun clearAlbumMedia(albumId: Long)

    @Query("SELECT COUNT(*) FROM album_media WHERE albumId = :albumId")
    fun getAlbumMediaCount(albumId: Long): Flow<Int>
}
