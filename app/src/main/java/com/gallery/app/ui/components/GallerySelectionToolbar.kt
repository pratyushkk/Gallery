package com.gallery.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySelectionTopBar(
    selectedCount: Int,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "$selectedCount selected",
                style = GalleryTypography.sectionHeader,
                color = GalleryColors.TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancel selection",
                    tint = GalleryColors.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(
                    imageVector = Icons.Outlined.SelectAll,
                    contentDescription = "Select all",
                    tint = GalleryColors.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GalleryColors.Background
        ),
        modifier = modifier
    )
}

@Composable
fun GallerySelectionBottomBar(
    onShare: () -> Unit,
    onMove: () -> Unit,
    onFavorite: () -> Unit,
    onLock: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GalleryColors.Background)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(GalleryColors.Divider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GalleryDimensions.bottomNavHeight)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionActionItem(icon = Icons.Outlined.Share, label = "Share", onClick = onShare)
            SelectionActionItem(icon = Icons.Outlined.DriveFileMove, label = "Move", onClick = onMove)
            SelectionActionItem(icon = Icons.Outlined.FavoriteBorder, label = "Favorite", onClick = onFavorite)
            SelectionActionItem(icon = Icons.Outlined.Lock, label = "Lock", onClick = onLock)
            SelectionActionItem(icon = Icons.Outlined.Delete, label = "Delete", onClick = onDelete)
            SelectionActionItem(icon = Icons.Default.MoreHoriz, label = "More", onClick = onMore)
        }
    }
}

@Composable
private fun SelectionActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GalleryColors.TextPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = GalleryTypography.caption,
            color = GalleryColors.TextPrimary
        )
    }
}
