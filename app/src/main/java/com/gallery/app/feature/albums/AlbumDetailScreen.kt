package com.gallery.app.feature.albums

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallery.app.ui.components.GalleryEmptyState
import com.gallery.app.ui.components.GalleryThumbnail
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    onBack: () -> Unit,
    onMediaClick: (Long) -> Unit,
    viewModel: AlbumDetailViewModel = viewModel(
        factory = AlbumDetailViewModel.Factory,
        key = albumId
    ),
    isDarkTheme: Boolean = true
) {
    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        containerColor = GalleryColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (uiState as? AlbumDetailUiState.Success)?.albumName ?: "Album",
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
                    containerColor = GalleryColors.Background,
                    titleContentColor = GalleryColors.TextPrimary,
                    navigationIconContentColor = GalleryColors.TextPrimary
                )
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is AlbumDetailUiState.Loading -> {
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
            is AlbumDetailUiState.Empty -> {
                GalleryEmptyState(
                    message = "No photos in this album",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is AlbumDetailUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GalleryDimensions.photoGridColumns),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = GalleryDimensions.spaceXxs,
                        end = GalleryDimensions.spaceXxs,
                        bottom = 16.dp
                    )
                ) {
                    items(items = state.media, key = { it.id }, contentType = { "thumbnail" }) { item ->
                        GalleryThumbnail(
                            item = item,
                            onClick = { onMediaClick(item.id) },
                            onLongClick = {}
                        )
                    }
                }
            }
        }
    }
}
