package com.gallery.app.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gallery_settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class GridDensity { COMPACT, NORMAL, COMFORTABLE }
enum class RetentionPeriod(val days: Int) {
    DAYS_7(7), DAYS_30(30), DAYS_60(60), NEVER(-1)
}

class AppSettings(private val context: Context) {
    
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GRID_DENSITY = stringPreferencesKey("grid_density")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val VIDEO_AUTOPLAY = booleanPreferencesKey("video_autoplay")
        val SHOW_HIDDEN_ALBUMS = booleanPreferencesKey("show_hidden_albums")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LOCKER_PIN_HASH = stringPreferencesKey("locker_pin_hash")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        val SECURE_RECENTS = booleanPreferencesKey("secure_recents")
        val RETENTION_PERIOD = stringPreferencesKey("retention_period")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        val METADATA_VISIBILITY = booleanPreferencesKey("metadata_visibility")
    }
    
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        try { ThemeMode.valueOf(prefs[THEME_MODE] ?: ThemeMode.DARK.name) }
        catch (_: Exception) { ThemeMode.DARK }
    }
    
    val gridDensity: Flow<GridDensity> = context.dataStore.data.map { prefs ->
        try { GridDensity.valueOf(prefs[GRID_DENSITY] ?: GridDensity.NORMAL.name) }
        catch (_: Exception) { GridDensity.NORMAL }
    }
    
    val confirmDelete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[CONFIRM_DELETE] ?: true
    }
    
    val videoAutoplay: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VIDEO_AUTOPLAY] ?: false
    }
    
    val showHiddenAlbums: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_HIDDEN_ALBUMS] ?: false
    }
    
    val sortOrder: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SORT_ORDER] ?: "DATE_DESC"
    }

    val sortOrderFlow: Flow<com.gallery.app.core.model.SortOrder> = context.dataStore.data.map { prefs ->
        try { com.gallery.app.core.model.SortOrder.valueOf(prefs[SORT_ORDER] ?: com.gallery.app.core.model.SortOrder.DATE_DESC.name) }
        catch (_: Exception) { com.gallery.app.core.model.SortOrder.DATE_DESC }
    }
    
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BIOMETRIC_ENABLED] ?: true
    }
    
    val autoLockTimeout: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AUTO_LOCK_TIMEOUT] ?: 30
    }
    
    val secureRecents: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SECURE_RECENTS] ?: false
    }
    
    val retentionPeriod: Flow<RetentionPeriod> = context.dataStore.data.map { prefs ->
        try { RetentionPeriod.valueOf(prefs[RETENTION_PERIOD] ?: RetentionPeriod.DAYS_30.name) }
        catch (_: Exception) { RetentionPeriod.DAYS_30 }
    }
    
    val reduceAnimations: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REDUCE_ANIMATIONS] ?: false
    }
    
    val metadataVisibility: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[METADATA_VISIBILITY] ?: true
    }
    
    suspend fun <T> setSetting(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { prefs ->
            prefs[key] = value
        }
    }
}
