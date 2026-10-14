package com.gallery.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.navigation.navArgument
import com.gallery.app.core.settings.AppSettings
import com.gallery.app.core.settings.ThemeMode
import kotlinx.coroutines.launch
import com.gallery.app.feature.albums.AlbumDetailScreen
import com.gallery.app.feature.albums.AlbumsScreen
import com.gallery.app.feature.locker.LockerScreen
import com.gallery.app.feature.photos.PhotosScreen
import com.gallery.app.feature.recyclebin.RecycleBinScreen
import com.gallery.app.feature.search.SearchScreen
import com.gallery.app.feature.settings.SettingsScreen
import com.gallery.app.feature.viewer.ViewerScreen
import com.gallery.app.ui.components.BottomNavTabItem
import com.gallery.app.ui.components.GalleryBottomNavigation
import com.gallery.app.ui.components.PermissionHandler
import com.gallery.app.ui.designsystem.GalleryIcons
import com.gallery.app.ui.designsystem.GalleryTheme

@Composable
fun GalleryApp() {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val themeMode by settings.themeMode.collectAsState(initial = ThemeMode.DARK)

    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    GalleryTheme {
        PermissionHandler {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val bottomNavItems = listOf(
                BottomNavTabItem(
                    route = Screen.Photos.route,
                    label = "Photos",
                    selectedIcon = GalleryIcons.PhotosFilled,
                    unselectedIcon = GalleryIcons.PhotosOutlined
                ),
                BottomNavTabItem(
                    route = Screen.Albums.route,
                    label = "Albums",
                    selectedIcon = GalleryIcons.AlbumsFilled,
                    unselectedIcon = GalleryIcons.AlbumsOutlined
                ),
                BottomNavTabItem(
                    route = Screen.Search.route,
                    label = "Search",
                    selectedIcon = GalleryIcons.SearchFilled,
                    unselectedIcon = GalleryIcons.SearchOutlined
                )
            )

            val mainPagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
            val coroutineScope = rememberCoroutineScope()

            val showBottomBar = currentRoute == Screen.Photos.route || currentRoute == null

            val currentTabRoute = when (mainPagerState.currentPage) {
                0 -> Screen.Photos.route
                1 -> Screen.Albums.route
                else -> Screen.Search.route
            }

            BackHandler(enabled = (currentRoute == Screen.Photos.route || currentRoute == null) && mainPagerState.currentPage != 0) {
                coroutineScope.launch {
                    mainPagerState.animateScrollToPage(0)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Photos.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Photos.route) {
                        HorizontalPager(
                            state = mainPagerState,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 1
                        ) { page ->
                            when (page) {
                                0 -> PhotosScreen(
                                    onMediaClick = { mediaId ->
                                        navController.navigate(Screen.Viewer.createRoute(mediaId))
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(Screen.Settings.route)
                                    },
                                    onNavigateToSearch = {
                                        coroutineScope.launch {
                                            mainPagerState.animateScrollToPage(2)
                                        }
                                    },
                                    onNavigateToLocker = {
                                        navController.navigate(Screen.Locker.route)
                                    },
                                    onNavigateToRecycleBin = {
                                        navController.navigate(Screen.RecycleBin.route)
                                    },
                                    isDarkTheme = isDarkTheme
                                )
                                1 -> AlbumsScreen(
                                    onAlbumClick = { albumId ->
                                        navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                                    },
                                    onLockerClick = {
                                        navController.navigate(Screen.Locker.route)
                                    },
                                    onRecycleBinClick = {
                                        navController.navigate(Screen.RecycleBin.route)
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(Screen.Settings.route)
                                    },
                                    onNavigateToSearch = {
                                        coroutineScope.launch {
                                            mainPagerState.animateScrollToPage(2)
                                        }
                                    },
                                    isDarkTheme = isDarkTheme
                                )
                                2 -> SearchScreen(
                                    onMediaClick = { mediaId ->
                                        navController.navigate(Screen.Viewer.createRoute(mediaId))
                                    },
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        }
                    }
                    composable(
                        route = Screen.Viewer.route,
                        arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: return@composable
                        ViewerScreen(
                            mediaId = mediaId,
                            onBack = { navController.popBackStack() },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    composable(
                        route = Screen.AlbumDetail.route,
                        arguments = listOf(navArgument("albumId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
                        AlbumDetailScreen(
                            albumId = albumId,
                            onBack = { navController.popBackStack() },
                            onMediaClick = { mediaId ->
                                navController.navigate(Screen.Viewer.createRoute(mediaId))
                            },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    composable(Screen.Locker.route) {
                        LockerScreen(
                            onBack = { navController.popBackStack() },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    composable(Screen.RecycleBin.route) {
                        RecycleBinScreen(
                            onBack = { navController.popBackStack() },
                            isDarkTheme = isDarkTheme
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    GalleryBottomNavigation(
                        items = bottomNavItems,
                        currentRoute = currentTabRoute,
                        onTabSelected = { route ->
                            coroutineScope.launch {
                                val targetPage = when (route) {
                                    Screen.Photos.route -> 0
                                    Screen.Albums.route -> 1
                                    Screen.Search.route -> 2
                                    else -> 0
                                }
                                mainPagerState.animateScrollToPage(targetPage)
                            }
                        }
                    )
                }
            }
        }
    }
}
