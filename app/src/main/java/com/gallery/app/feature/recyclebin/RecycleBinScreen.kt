package com.gallery.app.feature.recyclebin

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gallery.app.core.database.entity.RecycleBinEntity
import com.gallery.app.ui.components.GalleryEmptyState
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryShapes
import com.gallery.app.ui.designsystem.GalleryTypography
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    viewModel: RecycleBinViewModel = viewModel(factory = RecycleBinViewModel.Factory),
    isDarkTheme: Boolean = true
) {
    val items by viewModel.items.collectAsState()
    var showEmptyDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GalleryColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recycle Bin",
                        style = GalleryTypography.screenTitle,
                        color = GalleryColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GalleryColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GalleryColors.Background
                )
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = GalleryShapes.pill,
                        color = GalleryColors.SurfaceContainerHigh,
                        border = BorderStroke(1.dp, GalleryColors.Outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    items.forEach { viewModel.restoreItem(it) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Restore,
                                    contentDescription = null,
                                    tint = GalleryColors.TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Restore",
                                    style = GalleryTypography.actionButton,
                                    color = GalleryColors.TextPrimary
                                )
                            }

                            TextButton(
                                onClick = { showEmptyDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteForever,
                                    contentDescription = null,
                                    tint = GalleryColors.DestructiveRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delete all",
                                    style = GalleryTypography.actionButton,
                                    color = GalleryColors.DestructiveRed
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Supporting Banner Text (Matching Screen 12)
            Text(
                text = "Items in the Recycle Bin will be permanently deleted after 30 days.",
                style = GalleryTypography.bodyMedium,
                color = GalleryColors.TextSecondary,
                modifier = Modifier.padding(
                    horizontal = GalleryDimensions.screenHorizontalPadding,
                    vertical = 8.dp
                )
            )

            if (items.isEmpty()) {
                GalleryEmptyState(
                    message = "Recycle bin is empty",
                    icon = Icons.Outlined.Delete
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = items, key = { it.id }) { item ->
                        RecycleBinThumbnailItem(item = item)
                    }
                }
            }
        }
    }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("Delete all items?", style = GalleryTypography.sectionHeader) },
            text = {
                Text(
                    "All items in the Recycle Bin will be permanently deleted. This cannot be undone.",
                    style = GalleryTypography.bodyMedium,
                    color = GalleryColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyBin()
                        showEmptyDialog = false
                    }
                ) {
                    Text("Delete permanently", color = GalleryColors.DestructiveRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("Cancel", color = GalleryColors.TextSecondary)
                }
            },
            containerColor = GalleryColors.SurfaceContainerHigh,
            shape = GalleryShapes.dialog
        )
    }
}

@Composable
private fun RecycleBinThumbnailItem(
    item: RecycleBinEntity
) {
    val context = LocalContext.current
    val daysRemaining = remember(item.expiresAt) {
        val diff = item.expiresAt - System.currentTimeMillis()
        if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(1) else 0
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(GalleryColors.SurfaceContainer)
    ) {
        val cachedFile = remember(item.cachedFilePath) { File(item.cachedFilePath) }
        val imageRequest = remember(item.cachedFilePath) {
            ImageRequest.Builder(context)
                .data(cachedFile)
                .crossfade(false)
                .size(500)
                .precision(coil.size.Precision.INEXACT)
                .allowHardware(true)
                .allowRgb565(false)
                .build()
        }
        if (cachedFile.exists()) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Days remaining badge (Screen 12)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$daysRemaining days",
                color = Color.White,
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
    }
}
