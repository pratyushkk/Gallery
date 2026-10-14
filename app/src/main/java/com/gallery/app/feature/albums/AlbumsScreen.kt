package com.gallery.app.feature.albums

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallery.app.core.model.Album
import com.gallery.app.ui.components.*
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryShapes
import com.gallery.app.ui.designsystem.GalleryTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    onAlbumClick: (String) -> Unit,
    onLockerClick: () -> Unit,
    onRecycleBinClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: (() -> Unit)? = null,
    viewModel: AlbumsViewModel = viewModel(factory = AlbumsViewModel.Factory),
    isDarkTheme: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GalleryColors.Background,
        topBar = {
            GalleryTopBar(
                title = "Albums",
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Album",
                            tint = GalleryColors.TextPrimary
                        )
                    }
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
                                        "Sort by",
                                        color = GalleryColors.TextPrimary,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showSortDialog = true
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
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onLockerClick()
                                },
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Recycle Bin",
                                        color = GalleryColors.TextPrimary,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onRecycleBinClick()
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
                                        style = MaterialTheme.typography.bodyLarge
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
    ) { innerPadding ->
        when (val state = uiState) {
            is AlbumsUiState.Loading -> {
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
            is AlbumsUiState.Success -> {
                val allAlbums = state.userAlbums + state.systemAlbums

                if (allAlbums.isEmpty()) {
                    GalleryEmptyState(
                        message = "No albums found",
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GalleryDimensions.albumGridColumns),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding() + 80.dp,
                            start = GalleryDimensions.screenHorizontalPadding,
                            end = GalleryDimensions.screenHorizontalPadding
                        ),
                        horizontalArrangement = Arrangement.spacedBy(GalleryDimensions.albumGridSpacing),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                    // Section 1: "My albums" (View all)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GallerySectionHeader(
                            title = "My albums",
                            actionText = "View all",
                            onActionClick = { /* View all albums */ }
                        )
                    }

                    items(items = allAlbums, key = { it.id }, contentType = { "album" }) { album ->
                        GalleryAlbumCard(
                            title = album.name,
                            count = album.mediaCount,
                            coverUri = album.coverUri,
                            onClick = { onAlbumClick(album.id) }
                        )
                    }

                    // Section 2: "People & places"
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        GallerySectionHeader(title = "People & places")
                    }

                    // People card
                    item {
                        PeoplePlacesCard(
                            title = "People",
                            subtitle = "Find friends",
                            icon = Icons.Outlined.Person,
                            backgroundColor = GalleryColors.CategoryPeople,
                            onClick = { /* People filter */ }
                        )
                    }

                    // Places card
                    item {
                        PeoplePlacesCard(
                            title = "Places",
                            subtitle = "Locations",
                            icon = Icons.Outlined.Place,
                            backgroundColor = GalleryColors.CategoryPlaces,
                            onClick = { /* Places filter */ }
                        )
                    }

                    // Documents card
                    item {
                        PeoplePlacesCard(
                            title = "Documents",
                            subtitle = "Files",
                            icon = Icons.Outlined.Description,
                            backgroundColor = GalleryColors.CategoryDocuments,
                            onClick = { /* Documents filter */ }
                        )
                    }
                }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAlbumDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createAlbum(name)
                showCreateDialog = false
            }
        )
    }

    if (showSortDialog) {
        val sortOptions = listOf(
            AlbumSortOrder.DEFAULT to "Default",
            AlbumSortOrder.NAME to "Name (A–Z)",
            AlbumSortOrder.COUNT to "Item count (Most first)"
        )
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            containerColor = Color(0xFF222328),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Sort albums by",
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
}

@Composable
private fun PeoplePlacesCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(GalleryShapes.albumCard)
                .background(backgroundColor.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = GalleryTypography.cardTitle,
            color = GalleryColors.TextPrimary,
            maxLines = 1
        )

        Text(
            text = subtitle,
            style = GalleryTypography.cardSubtitle,
            color = GalleryColors.TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun CreateAlbumDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var albumName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New album", style = GalleryTypography.sectionHeader) },
        text = {
            OutlinedTextField(
                value = albumName,
                onValueChange = { albumName = it },
                label = { Text("Album name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (albumName.isNotBlank()) onConfirm(albumName.trim()) },
                enabled = albumName.isNotBlank()
            ) {
                Text("Create", color = GalleryColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GalleryColors.TextSecondary)
            }
        },
        containerColor = GalleryColors.SurfaceContainerHigh,
        shape = GalleryShapes.dialog
    )
}
