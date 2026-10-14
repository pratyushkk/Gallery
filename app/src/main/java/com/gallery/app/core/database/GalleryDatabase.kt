package com.gallery.app.core.database

import android.content.Context
import androidx.room.*
import com.gallery.app.core.database.dao.*
import com.gallery.app.core.database.entity.*
import com.gallery.app.core.database.converter.Converters

@Database(
    entities = [
        AlbumEntity::class,
        AlbumMediaCrossRef::class,
        FavoriteEntity::class,
        LockerItemEntity::class,
        RecycleBinEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun lockerDao(): LockerDao
    abstract fun recycleBinDao(): RecycleBinDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: GalleryDatabase? = null

        fun getInstance(context: Context): GalleryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GalleryDatabase::class.java,
                    "gallery_database"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
