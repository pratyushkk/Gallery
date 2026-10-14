package com.gallery.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gallery.app.core.model.MediaItem
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryShapes
import com.gallery.app.ui.designsystem.GalleryTypography
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryDetailsSheet(
    item: MediaItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val date = remember(item) {
        Date(item.dateTaken.takeIf { it > 0 } ?: item.dateAdded)
    }
    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy  h:mm a", Locale.getDefault()) }
    val formattedDate = remember(date) { dateFormatter.format(date) }

    // Calculate MP and size
    val megapixels = remember(item.width, item.height) {
        if (item.width > 0 && item.height > 0) {
            String.format("%.0f MP", (item.width.toLong() * item.height.toLong()) / 1_000_000.0)
        } else ""
    }
    val fileSizeFormatted = remember(item.size) {
        String.format("%.1f MB", item.size / (1024.0 * 1024.0))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = GalleryColors.SurfaceContainer,
        shape = GalleryShapes.bottomSheet,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = GalleryColors.TextTertiary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Header: Date & Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = GalleryTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = GalleryColors.TextPrimary
                )
                Text(
                    text = "Details",
                    style = GalleryTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = GalleryColors.AccentBlue
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Filename and storage path
            Text(
                text = item.displayName,
                style = GalleryTypography.cardSubtitle,
                color = GalleryColors.TextSecondary
            )
            Text(
                text = "/internal storage/${item.relativePath.ifEmpty { "DCIM/Camera" }}",
                style = GalleryTypography.caption,
                color = GalleryColors.TextTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Camera / Tech Details Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = GalleryShapes.card,
                color = GalleryColors.SurfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Galaxy Device",
                        style = GalleryTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = GalleryColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$megapixels  ${item.width} x ${item.height}  $fileSizeFormatted",
                        style = GalleryTypography.bodyMedium,
                        color = GalleryColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "f/1.8  1/1200 s  ISO 50",
                        style = GalleryTypography.caption,
                        color = GalleryColors.TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location preview box (matching reference)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = GalleryShapes.card,
                color = GalleryColors.SurfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Map mockup background with pin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C3E50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location Pin",
                            tint = GalleryColors.DestructiveRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.bucketName.ifEmpty { "Device Gallery" },
                        style = GalleryTypography.bodyMedium,
                        color = GalleryColors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add location button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Location hook */ }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add location",
                    tint = GalleryColors.AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add location",
                    style = GalleryTypography.bodyMedium,
                    color = GalleryColors.AccentBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
