package com.gallery.app.feature.search

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gallery.app.ui.components.*
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryShapes
import com.gallery.app.ui.designsystem.GalleryTypography

@Composable
fun SearchScreen(
    onMediaClick: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
    isDarkTheme: Boolean = true
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val recentSearches = listOf(
        "Travel",
        "Screenshots",
        "Beach",
        "Birthday"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GalleryColors.Background)
            .statusBarsPadding()
    ) {
        // Large Title
        Text(
            text = "Search",
            style = GalleryTypography.screenTitle,
            color = GalleryColors.TextPrimary,
            modifier = Modifier.padding(
                start = GalleryDimensions.screenHorizontalPadding,
                end = GalleryDimensions.screenHorizontalPadding,
                top = GalleryDimensions.largeTitleTopPadding,
                bottom = GalleryDimensions.largeTitleBottomPadding
            )
        )

        // Rounded Pill Search Field
        Box(
            modifier = Modifier.padding(horizontal = GalleryDimensions.screenHorizontalPadding)
        ) {
            GallerySearchField(
                query = query,
                onQueryChange = { viewModel.updateQuery(it) },
                onSearch = {
                    viewModel.search()
                    keyboardController?.hide()
                },
                onClear = { viewModel.clearQuery() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (query.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Category grid (2 rows of 3 categories)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GalleryDimensions.screenHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SearchCategoryCard(
                                label = "People",
                                icon = Icons.Outlined.Person,
                                iconColor = GalleryColors.CategoryPeople,
                                onClick = {
                                    viewModel.updateQuery("people")
                                    viewModel.search()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SearchCategoryCard(
                                label = "Places",
                                icon = Icons.Outlined.Place,
                                iconColor = GalleryColors.CategoryPlaces,
                                onClick = {
                                    viewModel.updateQuery("places")
                                    viewModel.search()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SearchCategoryCard(
                                label = "Videos",
                                icon = Icons.Outlined.Videocam,
                                iconColor = GalleryColors.CategoryVideos,
                                onClick = {
                                    viewModel.updateQuery("videos")
                                    viewModel.search()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SearchCategoryCard(
                                label = "Screenshots",
                                icon = Icons.Outlined.Screenshot,
                                iconColor = GalleryColors.CategoryScreenshots,
                                onClick = {
                                    viewModel.updateQuery("screenshots")
                                    viewModel.search()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SearchCategoryCard(
                                label = "Favorites",
                                icon = Icons.Outlined.Star,
                                iconColor = GalleryColors.CategoryFavorites,
                                onClick = {
                                    viewModel.updateQuery("favorites")
                                    viewModel.search()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SearchCategoryCard(
                                label = "Documents",
                                icon = Icons.Outlined.Description,
                                iconColor = GalleryColors.CategoryDocuments,
                                onClick = {
                                    viewModel.updateQuery("documents")
                                    viewModel.search()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    GallerySectionHeader(
                        title = "Recent searches",
                        actionText = "Clear all",
                        onActionClick = { /* Clear search history */ }
                    )
                }

                // Recent searches list rows matching Screen 3
                items(recentSearches) { term ->
                    RecentSearchRow(
                        query = term,
                        onClick = {
                            viewModel.updateQuery(term)
                            viewModel.search()
                        }
                    )
                }
            }
        } else if (isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = GalleryColors.Primary,
                    strokeWidth = 2.dp
                )
            }
        } else if (results.isEmpty()) {
            GalleryEmptyState(
                message = "No results found",
                icon = Icons.Outlined.SearchOff
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "${results.size} results",
                    style = GalleryTypography.caption,
                    color = GalleryColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(GalleryDimensions.photoGridColumns),
                    contentPadding = PaddingValues(
                        start = GalleryDimensions.spaceXxs,
                        end = GalleryDimensions.spaceXxs,
                        bottom = 90.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = results, key = { it.id }, contentType = { "thumbnail" }) { item ->
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

@Composable
private fun SearchCategoryCard(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = GalleryShapes.categoryChip,
        color = GalleryColors.SurfaceContainer,
        modifier = modifier
            .height(GalleryDimensions.searchCategoryChipHeight)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = GalleryTypography.caption,
                color = GalleryColors.TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentSearchRow(
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = GalleryDimensions.screenHorizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail icon box
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GalleryColors.SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = GalleryColors.TextTertiary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = query,
            style = GalleryTypography.bodyLarge,
            color = GalleryColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            tint = GalleryColors.TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}
