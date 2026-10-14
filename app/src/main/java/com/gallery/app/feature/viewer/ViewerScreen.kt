package com.gallery.app.feature.viewer

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gallery.app.core.model.MediaItem
import com.gallery.app.ui.components.GalleryDetailsSheet
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryMotion
import com.gallery.app.ui.designsystem.GalleryTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    mediaId: Long,
    onBack: () -> Unit,
    viewModel: ViewerViewModel = viewModel(factory = ViewerViewModel.Factory),
    isDarkTheme: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    var showControls by remember { mutableStateOf(true) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showLockDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val deleteLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.deleteIntentSender.collect { intentSender ->
            if (intentSender != null) {
                deleteLauncher.launch(
                    androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
                )
            }
        }
    }

    LaunchedEffect(mediaId) {
        viewModel.loadMedia(mediaId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GalleryColors.Background)
    ) {
        when (val state = uiState) {
            is ViewerUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GalleryColors.Primary, strokeWidth = 2.dp)
                }
            }
            is ViewerUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Could not load media", color = GalleryColors.TextSecondary)
                }
            }
            is ViewerUiState.Success -> {
                val allMedia = state.allMedia
                val initialPage = allMedia.indexOfFirst { it.id == mediaId }.coerceAtLeast(0)
                val pagerState = rememberPagerState(
                    initialPage = initialPage,
                    pageCount = { allMedia.size }
                )
                val filmstripState = rememberLazyListState()

                val currentItem = allMedia.getOrNull(pagerState.currentPage)

                LaunchedEffect(pagerState.currentPage) {
                    val item = allMedia.getOrNull(pagerState.currentPage)
                    if (item != null) {
                        viewModel.updateCurrentItem(item)
                        filmstripState.animateScrollToItem(
                            (pagerState.currentPage - 2).coerceAtLeast(0)
                        )
                    }
                }

                // Fullscreen Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    val item = allMedia[page]
                    if (item.isVideo) {
                        ViewerVideoPlayer(
                            item = item,
                            onTap = { showControls = !showControls }
                        )
                    } else {
                        ViewerZoomableImage(
                            item = item,
                            onTap = { showControls = !showControls }
                        )
                    }
                }

                // Top Controls (Matching Screen 4)
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(animationSpec = GalleryMotion.standardTween()),
                    exit = fadeOut(animationSpec = GalleryMotion.standardTween()),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                ) {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = GalleryColors.TextPrimary
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) GalleryColors.FavoriteRed else GalleryColors.TextPrimary
                                )
                            }
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More",
                                        tint = GalleryColors.TextPrimary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false },
                                    modifier = Modifier
                                        .widthIn(min = 180.dp)
                                        .border(0.5.dp, Color(0xFF383A42), RoundedCornerShape(18.dp)),
                                    shape = RoundedCornerShape(18.dp),
                                    containerColor = Color(0xFF24252A),
                                    tonalElevation = 6.dp,
                                    shadowElevation = 8.dp
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Details",
                                                color = GalleryColors.TextPrimary,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showDetailsSheet = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                    )
                                    HorizontalDivider(
                                        color = Color(0xFF363840),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Move to Locker",
                                                color = GalleryColors.TextPrimary,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showLockDialog = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }

                // Bottom Filmstrip & Controls (Matching Screen 4)
                AnimatedVisibility(
                    visible = showControls,
                    enter = GalleryMotion.enterSlideUp,
                    exit = GalleryMotion.exitSlideDown,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GalleryColors.Background.copy(alpha = 0.85f))
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        // Filmstrip Thumbnail Carousel
                        if (allMedia.size > 1) {
                            LazyRow(
                                state = filmstripState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                itemsIndexed(allMedia, key = { _, item -> item.id }) { index, item ->
                                    val isCurrent = index == pagerState.currentPage
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCurrent) 38.dp else 32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                width = if (isCurrent) 2.dp else 0.dp,
                                                color = if (isCurrent) Color.White else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            }
                                    ) {
                                        val filmstripRequest = remember(item.uri) {
                                            ImageRequest.Builder(context)
                                                .data(item.uri)
                                                .memoryCacheKey("${item.uri}_filmstrip")
                                                .size(160)
                                                .precision(coil.size.Precision.INEXACT)
                                                .allowHardware(true)
                                                .allowRgb565(false)
                                                .crossfade(false)
                                                .build()
                                        }
                                        AsyncImage(
                                            model = filmstripRequest,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        // Divider
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(GalleryColors.Divider)
                        )

                        // Bottom Action Bar: Share, Delete, More (NO EDIT)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(GalleryDimensions.bottomNavHeight),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Share
                            ViewerActionItem(
                                icon = Icons.Outlined.Share,
                                label = "Share",
                                onClick = {
                                    currentItem?.let {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = it.mimeType
                                            putExtra(Intent.EXTRA_STREAM, it.uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share"))
                                    }
                                }
                            )

                            // Delete
                            ViewerActionItem(
                                icon = Icons.Outlined.Delete,
                                label = "Delete",
                                onClick = {
                                    currentItem?.let {
                                        viewModel.deleteMedia(it)
                                    }
                                }
                            )

                            // More / Details
                            ViewerActionItem(
                                icon = Icons.Default.MoreHoriz,
                                label = "More",
                                onClick = { showDetailsSheet = true }
                            )
                        }
                    }
                }

                // Photo Details Sheet (Screen 9)
                if (showDetailsSheet && currentItem != null) {
                    GalleryDetailsSheet(
                        item = currentItem,
                        onDismiss = { showDetailsSheet = false }
                    )
                }

                if (showLockDialog && currentItem != null) {
                    AlertDialog(
                        onDismissRequest = { showLockDialog = false },
                        containerColor = Color(0xFF222328),
                        shape = RoundedCornerShape(24.dp),
                        icon = {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = GalleryColors.LockerGold,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        title = {
                            Text(
                                "Move to Locker?",
                                style = MaterialTheme.typography.titleLarge,
                                color = GalleryColors.TextPrimary
                            )
                        },
                        text = {
                            Text(
                                "This photo will be encrypted and hidden from other apps. You can view it in the Locker with your biometric or PIN.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GalleryColors.TextSecondary
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showLockDialog = false
                                    viewModel.moveToLocker(currentItem) {
                                        android.widget.Toast.makeText(context, "Item moved to Locker", android.widget.Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                }
                            ) {
                                Text(
                                    "Move to Locker",
                                    color = GalleryColors.Primary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLockDialog = false }) {
                                Text("Cancel", color = GalleryColors.TextSecondary)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GalleryColors.TextPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = GalleryTypography.caption,
            color = GalleryColors.TextPrimary
        )
    }
}

@Composable
private fun ViewerZoomableImage(
    item: MediaItem,
    onTap: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember(item.id) { mutableFloatStateOf(1f) }
    var offset by remember(item.id) { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(item.id) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            .pointerInput(item.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.size
                        if (pointerCount > 1) {
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            if (newScale > 1.05f) {
                                scale = newScale
                                offset = Offset(offset.x + pan.x, offset.y + pan.y)
                            } else {
                                scale = 1f
                                offset = Offset.Zero
                            }
                            event.changes.forEach { it.consume() }
                        } else if (scale > 1.05f) {
                            val pan = event.calculatePan()
                            offset = Offset(offset.x + pan.x, offset.y + pan.y)
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val fullImageRequest = remember(item.uri) {
            ImageRequest.Builder(context)
                .data(item.uri)
                .memoryCacheKey("${item.uri}_full")
                .size(coil.size.Size.ORIGINAL)
                .precision(coil.size.Precision.INEXACT)
                .allowHardware(true)
                .allowRgb565(false)
                .placeholderMemoryCacheKey("${item.uri}_thumb")
                .crossfade(150)
                .build()
        }
        AsyncImage(
            model = fullImageRequest,
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

@Composable
private fun ViewerVideoPlayer(
    item: MediaItem,
    onTap: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = Media3Item.fromUri(item.uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
