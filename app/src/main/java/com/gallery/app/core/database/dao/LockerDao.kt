package com.gallery.app.core.database.dao

import androidx.room.*
import com.gallery.app.core.database.entity.LockerItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockerDao {
    @Query("SELECT * FROM locker_items ORDER BY dateAdded DESC")
    fun getAllLockerItems(): Flow<List<LockerItemEntity>>

    @Query("SELECT * FROM locker_items WHERE id = :id")
    suspend fun getLockerItemById(id: Long): LockerItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockerItem(item: LockerItemEntity): Long

    @Delete
    suspend fun deleteLockerItem(item: LockerItemEntity)

    @Query("DELETE FROM locker_items WHERE id = :id")
    suspend fun deleteLockerItemById(id: Long)

    @Query("SELECT COUNT(*) FROM locker_items")
    fun getLockerItemCount(): Flow<Int>

    @Query("SELECT * FROM locker_items WHERE originalMediaId = :mediaId")
    suspend fun getLockerItemByMediaId(mediaId: Long): LockerItemEntity?
}
