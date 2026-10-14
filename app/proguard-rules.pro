# ProGuard rules for Gallery app release build

-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

-keep class coil.** { *; }
-dontwarn coil.**

-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# App database entities and models
-keep class com.gallery.app.core.database.entity.** { *; }
-keep class com.gallery.app.core.database.converter.** { *; }
-keep class com.gallery.app.core.database.dao.** { *; }
-keep class com.gallery.app.core.model.** { *; }
-keep class com.gallery.app.core.settings.** { *; }
