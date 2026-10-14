package com.gallery.app.feature.locker

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gallery.app.core.database.entity.LockerItemEntity
import com.gallery.app.core.security.BiometricHelper
import com.gallery.app.ui.components.GalleryEmptyState
import com.gallery.app.ui.components.GallerySelectionTopBar
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryShapes
import com.gallery.app.ui.designsystem.GalleryTypography
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LockerScreen(
    onBack: () -> Unit,
    viewModel: LockerViewModel = viewModel(factory = LockerViewModel.Factory),
    isDarkTheme: Boolean = true
) {
    val context = LocalContext.current
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val lockerItems by viewModel.lockerItems.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    val biometricHelper = remember { BiometricHelper(context) }
    var showPinDialog by remember { mutableStateOf(false) }
    var previewItem by remember { mutableStateOf<LockerItemEntity?>(null) }

    fun triggerAuth() {
        val activity = context as? FragmentActivity
        if (activity != null && biometricHelper.canAuthenticate()) {
            biometricHelper.authenticate(
                activity = activity,
                onSuccess = { viewModel.authenticate() },
                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                onFailed = { Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show() }
            )
        } else {
            viewModel.authenticate()
        }
    }

    Scaffold(
        containerColor = GalleryColors.Background,
        topBar = {
            if (isAuthenticated && isSelectionMode) {
                GallerySelectionTopBar(
                    selectedCount = selectedIds.size,
                    onCancel = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll() }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "Locker",
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
                    actions = {
                        if (isAuthenticated) {
                            if (lockerItems.isNotEmpty()) {
                                IconButton(onClick = { viewModel.enterSelectionMode() }) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Select",
                                        tint = GalleryColors.TextPrimary
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.lock() }) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Lock",
                                    tint = GalleryColors.TextPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GalleryColors.Background
                    )
                )
            }
        },
        bottomBar = {
            if (isAuthenticated && isSelectionMode && selectedIds.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = GalleryColors.SurfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.restoreSelected { count ->
                                    Toast.makeText(context, "$count item(s) restored to Gallery", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GalleryColors.Primary),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore to Gallery")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.deleteSelected()
                                Toast.makeText(context, "Items permanently deleted", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GalleryColors.DestructiveRed),
                            border = BorderStroke(1.dp, GalleryColors.DestructiveRed),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (!isAuthenticated) {
            // Locker Auth Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GalleryColors.SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Unlock to view",
                        style = GalleryTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                        color = GalleryColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "your private photos and videos",
                        style = GalleryTypography.bodyMedium,
                        color = GalleryColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Box(
                        modifier = Modifier
                            .size(GalleryDimensions.fingerprintCircleSize)
                            .clip(CircleShape)
                            .background(GalleryColors.LockerBlue)
                            .clickable { triggerAuth() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Authenticate with fingerprint",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Use fingerprint",
                        style = GalleryTypography.caption,
                        color = GalleryColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        modifier = Modifier
                            .width(180.dp)
                            .height(44.dp)
                            .clickable { showPinDialog = true },
                        shape = GalleryShapes.pill,
                        color = GalleryColors.SurfaceContainer,
                        border = BorderStroke(1.dp, GalleryColors.Outline)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Use PIN",
                                style = GalleryTypography.actionButton,
                                color = GalleryColors.TextPrimary
                            )
                        }
                    }
                }
            }
        } else {
            // Locker Contents (Authenticated)
            if (lockerItems.isEmpty()) {
                GalleryEmptyState(
                    message = "No items in Locker",
                    icon = Icons.Outlined.Lock,
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GalleryDimensions.photoGridColumns),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() + 80.dp,
                        start = GalleryDimensions.spaceXxs,
                        end = GalleryDimensions.spaceXxs
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = lockerItems, key = { it.id }) { item ->
                        val isSelected = item.id in selectedIds
                        val decryptedFile = remember(item.id) { viewModel.getDecryptedFile(item) }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(GalleryDimensions.photoGridSpacing / 2)
                                .clip(GalleryShapes.thumbnail)
                                .background(GalleryColors.SurfaceContainer)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(item.id)
                                        } else {
                                            previewItem = item
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.enterSelectionMode()
                                        viewModel.toggleSelection(item.id)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(decryptedFile)
                                    .size(500)
                                    .precision(coil.size.Precision.INEXACT)
                                    .allowHardware(true)
                                    .allowRgb565(false)
                                    .crossfade(100)
                                    .build(),
                                contentDescription = item.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GalleryColors.SelectionOverlay)
                                )
                            }

                            if (isSelectionMode) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                        contentDescription = null,
                                        tint = if (isSelected) GalleryColors.SelectionBlue else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            if (item.mimeType.startsWith("video/")) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Preview Dialog
    previewItem?.let { item ->
        val decryptedFile = remember(item.id) { viewModel.getDecryptedFile(item) }
        AlertDialog(
            onDismissRequest = { previewItem = null },
            containerColor = Color(0xFF222328),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = item.displayName.ifEmpty { "Locked Item" },
                    style = MaterialTheme.typography.titleMedium,
                    color = GalleryColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(decryptedFile)
                                .build(),
                            contentDescription = item.displayName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Size: ${Formatter.formatFileSize(context, item.size)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GalleryColors.TextSecondary
                        )
                        Text(
                            text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(item.dateAdded)),
                            style = MaterialTheme.typography.bodySmall,
                            color = GalleryColors.TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toRestore = item
                        previewItem = null
                        viewModel.restoreLockerItem(toRestore) {
                            Toast.makeText(context, "Restored to Gallery", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GalleryColors.Primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Outlined.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore to Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val toDelete = item
                        previewItem = null
                        viewModel.deleteLockerItem(toDelete)
                        Toast.makeText(context, "Item deleted permanently", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = GalleryColors.DestructiveRed)
                }
            }
        )
    }

    if (showPinDialog) {
        var pin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Enter PIN", style = GalleryTypography.sectionHeader) },
            text = {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    singleLine = true,
                    placeholder = { Text("6-digit PIN") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.authenticate()
                        showPinDialog = false
                    }
                ) {
                    Text("Unlock", color = GalleryColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = GalleryColors.TextSecondary)
                }
            },
            containerColor = GalleryColors.SurfaceContainerHigh,
            shape = GalleryShapes.dialog
        )
    }
}
