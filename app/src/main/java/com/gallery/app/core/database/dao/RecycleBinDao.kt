package com.gallery.app.core.database.dao

import androidx.room.*
import com.gallery.app.core.database.entity.RecycleBinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecycleBinDao {
    @Query("SELECT * FROM recycle_bin ORDER BY deletedAt DESC")
    fun getAllRecycleBinItems(): Flow<List<RecycleBinEntity>>

    @Query("SELECT * FROM recycle_bin WHERE id = :id")
    suspend fun getRecycleBinItemById(id: Long): RecycleBinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecycleBinItem(item: RecycleBinEntity): Long

    @Delete
    suspend fun deleteRecycleBinItem(item: RecycleBinEntity)

    @Query("DELETE FROM recycle_bin WHERE id = :id")
    suspend fun deleteRecycleBinItemById(id: Long)

    @Query("DELETE FROM recycle_bin WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredItems(currentTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM recycle_bin")
    suspend fun emptyRecycleBin()

    @Query("SELECT COUNT(*) FROM recycle_bin")
    fun getRecycleBinCount(): Flow<Int>
}
