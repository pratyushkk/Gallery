plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}

allprojects {
    val isLocalMachine = System.getProperty("user.name") == "PRATYUSH"
    val localTemp = file("C:/Users/PRATYUSH/AppData/Local/Temp/gallery_build/${project.name}")
    if (isLocalMachine && localTemp.parentFile.exists()) {
        layout.buildDirectory.set(localTemp)
    }
}
