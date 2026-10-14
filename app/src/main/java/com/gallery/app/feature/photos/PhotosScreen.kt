package com.gallery.app.feature.photos

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallery.app.core.model.DateGroup
import com.gallery.app.core.model.MediaItem
import com.gallery.app.core.model.SortOrder
import com.gallery.app.core.settings.GridDensity
import com.gallery.app.ui.components.*
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    onMediaClick: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: (() -> Unit)? = null,
    onNavigateToLocker: (() -> Unit)? = null,
    onNavigateToRecycleBin: (() -> Unit)? = null,
    viewModel: PhotosViewModel = viewModel(factory = PhotosViewModel.Factory),
    isDarkTheme: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()
    val currentGridDensity by viewModel.gridDensity.collectAsState()

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(false) }
    var showLockDialog by remember { mutableStateOf(false) }

    val columnCount = when (currentGridDensity) {
        GridDensity.COMFORTABLE -> 3
        GridDensity.NORMAL -> 4
        GridDensity.COMPACT -> 5
    }

    val deleteLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.clearSelection()
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

    val gridState = rememberLazyGridState()

    Scaffold(
        containerColor = GalleryColors.Background,
        topBar = {
            if (isSelectionMode) {
                GallerySelectionTopBar(
                    selectedCount = selectedIds.size,
                    onCancel = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll() }
                )
            } else {
                GalleryTopBar(
                    title = "Photos",
                    actions = {
                        IconButton(onClick = { onNavigateToSearch?.invoke() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = GalleryColors.TextPrimary
                            )
                        }
                        Box {
                            IconButton(onClick = { showOverflowMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = GalleryColors.TextPrimary
                                )
                            }
                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false },
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
                                            "Select",
                                            color = GalleryColors.TextPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.enterSelectionMode()
                                    },
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Sort by",
                                            color = GalleryColors.TextPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        showSortDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "View",
                                            color = GalleryColors.TextPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        showViewDialog = true
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
                                            "Locker",
                                            color = GalleryColors.TextPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onNavigateToLocker?.invoke()
                                    },
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Recycle Bin",
                                            color = GalleryColors.TextPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onNavigateToRecycleBin?.invoke()
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
                                            "Settings",
                                            color = GalleryColors.TextPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onNavigateToSettings()
                                    },
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isSelectionMode && selectedIds.isNotEmpty()) {
                GallerySelectionBottomBar(
                    onShare = {
                        val selectedItems = (uiState as? PhotosUiState.Success)?.let { state ->
                            state.dateGroups.flatMap { it.items }.filter { it.id in selectedIds }
                        } ?: emptyList()
                        if (selectedItems.isNotEmpty()) {
                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "image/*"
                                putParcelableArrayListExtra(
                                    Intent.EXTRA_STREAM,
                                    ArrayList(selectedItems.map { it.uri })
                                )
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share"))
                        }
                    },
                    onMove = { /* Move to album */ },
                    onFavorite = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleFavoriteSelected()
                    },
                    onLock = {
                        showLockDialog = true
                    },
                    onDelete = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteSelected()
                    },
                    onMore = { /* More selection options */ }
                )
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is PhotosUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GalleryColors.Primary,
                        strokeWidth = 2.dp
                    )
                }
            }
            is PhotosUiState.Empty -> {
                GalleryEmptyState(
                    message = "No photos yet",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is PhotosUiState.Success -> {
                val allItems = remember(state.dateGroups) {
                    state.dateGroups.flatMap { it.items }
                }

                LaunchedEffect(gridState, allItems) {
                    val imageLoader = coil.Coil.imageLoader(context)
                    var lastPrefetchChunk = -1

                    snapshotFlow { gridState.firstVisibleItemIndex }
                        .collect { firstIndex ->
                            val currentChunk = firstIndex / 16
                            if (currentChunk != lastPrefetchChunk) {
                                lastPrefetchChunk = currentChunk
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val prefetchStart = (firstIndex + 8).coerceAtMost(allItems.size)
                                    val prefetchEnd = (prefetchStart + 24).coerceAtMost(allItems.size)
                                    for (i in prefetchStart until prefetchEnd) {
                                        val req = coil.request.ImageRequest.Builder(context)
                                            .data(allItems[i].uri)
                                            .memoryCacheKey("${allItems[i].uri}_thumb")
                                            .size(500)
                                            .precision(coil.size.Precision.INEXACT)
                                            .allowHardware(true)
                                            .allowRgb565(false)
                                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                            .build()
                                        imageLoader.enqueue(req)
                                    }
                                }
                            }
                        }
                }

                var pinchScale by remember { mutableFloatStateOf(1f) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    state = gridState,
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() + 80.dp,
                        start = GalleryDimensions.spaceXxs,
                        end = GalleryDimensions.spaceXxs
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                if (zoom != 1f) {
                                    pinchScale *= zoom
                                    if (pinchScale > 1.25f) {
                                        viewModel.zoomGrid(zoomIn = true)
                                        pinchScale = 1f
                                    } else if (pinchScale < 0.75f) {
                                        viewModel.zoomGrid(zoomIn = false)
                                        pinchScale = 1f
                                    }
                                }
                            }
                        }
                ) {
                    state.dateGroups.forEach { group ->
                        item(
                            key = "header_${group.label}_${group.date}",
                            span = { GridItemSpan(maxLineSpan) },
                            contentType = "header"
                        ) {
                            GallerySectionHeader(
                                title = group.label,
                                actionText = if (isSelectionMode) "Select all" else null,
                                onActionClick = if (isSelectionMode) {
                                    { viewModel.toggleGroupSelection(group) }
                                } else null
                            )
                        }

                        items(
                            items = group.items,
                            key = { it.id },
                            contentType = { "thumbnail" }
                        ) { item ->
                            GalleryThumbnail(
                                item = item,
                                isSelected = item.id in selectedIds,
                                isSelectionMode = isSelectionMode,
                                isFavorite = item.id in state.favoriteIds,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.toggleSelection(item.id)
                                    } else {
                                        onMediaClick(item.id)
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.enterSelectionMode()
                                    viewModel.toggleSelection(item.id)
                                }
                            )
                        }
                    }
                }
            }
            is PhotosUiState.Error -> {
                GalleryEmptyState(
                    message = state.message,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (showSortDialog) {
        val sortOptions = listOf(
            SortOrder.DATE_DESC to "Date (Newest first)",
            SortOrder.DATE_ASC to "Date (Oldest first)",
            SortOrder.NAME_ASC to "Name (A–Z)",
            SortOrder.NAME_DESC to "Name (Z–A)",
            SortOrder.SIZE_DESC to "Size (Largest first)",
            SortOrder.SIZE_ASC to "Size (Smallest first)"
        )
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            containerColor = Color(0xFF222328),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Sort by",
                    style = MaterialTheme.typography.titleLarge,
                    color = GalleryColors.TextPrimary
                )
            },
            text = {
                Column {
                    sortOptions.forEach { (order, label) ->
                        val selected = currentSortOrder == order
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSortOrder(order)
                                    showSortDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = {
                                    viewModel.setSortOrder(order)
                                    showSortDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GalleryColors.Primary,
                                    unselectedColor = GalleryColors.TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected) GalleryColors.Primary else GalleryColors.TextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Cancel", color = GalleryColors.Primary)
                }
            }
        )
    }

    if (showViewDialog) {
        val viewOptions = listOf(
            GridDensity.COMFORTABLE to "Large (3 columns)",
            GridDensity.NORMAL to "Medium (4 columns)",
            GridDensity.COMPACT to "Small (5 columns)"
        )
        AlertDialog(
            onDismissRequest = { showViewDialog = false },
            containerColor = Color(0xFF222328),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Grid view",
                    style = MaterialTheme.typography.titleLarge,
                    color = GalleryColors.TextPrimary
                )
            },
            text = {
                Column {
                    viewOptions.forEach { (density, label) ->
                        val selected = currentGridDensity == density
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setGridDensity(density)
                                    showViewDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = {
                                    viewModel.setGridDensity(density)
                                    showViewDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GalleryColors.Primary,
                                    unselectedColor = GalleryColors.TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected) GalleryColors.Primary else GalleryColors.TextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViewDialog = false }) {
                    Text("Cancel", color = GalleryColors.Primary)
                }
            }
        )
    }

    if (showLockDialog) {
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
                    "The selected ${selectedIds.size} item(s) will be encrypted and hidden from other apps. You can view them in the Locker with your biometric or PIN.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GalleryColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLockDialog = false
                        viewModel.moveSelectedToLocker { count ->
                            Toast.makeText(context, "$count item(s) moved to Locker", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        "Move to Locker",
                        color = GalleryColors.Primary,
                        fontWeight = FontWeight.Bold
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
