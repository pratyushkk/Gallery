package com.gallery.app.ui

sealed class Screen(val route: String) {
    object Photos : Screen("photos")
    object Albums : Screen("albums")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Locker : Screen("locker")
    object RecycleBin : Screen("recycle_bin")
    
    object Viewer : Screen("viewer/{mediaId}") {
        fun createRoute(mediaId: Long) = "viewer/$mediaId"
    }
    
    object AlbumDetail : Screen("album/{albumId}") {
        fun createRoute(albumId: String) = "album/$albumId"
    }
}
