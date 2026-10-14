package com.gallery.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gallery.app.core.model.SortOrder
import com.gallery.app.core.settings.AppSettings
import com.gallery.app.core.settings.GridDensity
import com.gallery.app.core.settings.RetentionPeriod
import com.gallery.app.core.settings.ThemeMode
import com.gallery.app.ui.components.GallerySettingsRow
import com.gallery.app.ui.designsystem.GalleryColors
import com.gallery.app.ui.designsystem.GalleryDimensions
import com.gallery.app.ui.designsystem.GalleryTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()

    val themeMode by settings.themeMode.collectAsState(initial = ThemeMode.DARK)
    val retentionPeriod by settings.retentionPeriod.collectAsState(initial = RetentionPeriod.DAYS_30)
    val biometricEnabled by settings.biometricEnabled.collectAsState(initial = true)
    val gridDensity by settings.gridDensity.collectAsState(initial = GridDensity.NORMAL)
    val sortOrder by settings.sortOrderFlow.collectAsState(initial = com.gallery.app.core.model.SortOrder.DATE_DESC)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showRetentionDialog by remember { mutableStateOf(false) }
    var showGalleryOptionsDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GalleryColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Appearance (Screen 13)
            GallerySettingsRow(
                title = "Appearance",
                subtitle = "Dark mode, theme, layout",
                icon = Icons.Outlined.DarkMode,
                iconBackgroundColor = Color(0xFF5E5CE6),
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider(
                color = GalleryColors.Divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 72.dp)
            )

            // 2. Gallery (Screen 13)
            GallerySettingsRow(
                title = "Gallery",
                subtitle = "Sorting, view options",
                icon = Icons.Outlined.PhotoLibrary,
                iconBackgroundColor = Color(0xFF34C759),
                onClick = { showGalleryOptionsDialog = true }
            )

            HorizontalDivider(
                color = GalleryColors.Divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 72.dp)
            )

            // 3. Locker (Screen 13)
            GallerySettingsRow(
                title = "Locker",
                subtitle = "Security, auto-lock",
                icon = Icons.Outlined.Lock,
                iconBackgroundColor = Color(0xFF0A84FF),
                onClick = {
                    scope.launch {
                        settings.setSetting(AppSettings.BIOMETRIC_ENABLED, !biometricEnabled)
                    }
                },
                trailingContent = {
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { value ->
                            scope.launch {
                                settings.setSetting(AppSettings.BIOMETRIC_ENABLED, value)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GalleryColors.Primary
                        )
                    )
                }
            )

            HorizontalDivider(
                color = GalleryColors.Divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 72.dp)
            )

            // 4. Recycle Bin (Screen 13)
            GallerySettingsRow(
                title = "Recycle Bin",
                subtitle = "Retention period (${retentionPeriod.days} days)",
                icon = Icons.Outlined.Delete,
                iconBackgroundColor = Color(0xFF636366),
                onClick = { showRetentionDialog = true }
            )

            HorizontalDivider(
                color = GalleryColors.Divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 72.dp)
            )

            // 5. Privacy (Screen 13)
            GallerySettingsRow(
                title = "Privacy",
                subtitle = "Permissions, metadata",
                icon = Icons.Outlined.Security,
                iconBackgroundColor = Color(0xFF30D158),
                onClick = { /* Privacy details */ }
            )

            HorizontalDivider(
                color = GalleryColors.Divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 72.dp)
            )

            // 6. About (Screen 13)
            GallerySettingsRow(
                title = "About",
                subtitle = "Version 1.0.0, licenses",
                icon = Icons.Outlined.Info,
                iconBackgroundColor = Color(0xFF8E8E93),
                onClick = { /* About dialog */ }
            )
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme", style = GalleryTypography.sectionHeader) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    scope.launch {
                                        settings.setSetting(AppSettings.THEME_MODE, mode.name)
                                        showThemeDialog = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = GalleryColors.SurfaceContainerHigh
        )
    }

    if (showRetentionDialog) {
        AlertDialog(
            onDismissRequest = { showRetentionDialog = false },
            title = { Text("Recycle Bin Retention", style = GalleryTypography.sectionHeader) },
            text = {
                Column {
                    listOf(
                        RetentionPeriod.DAYS_7,
                        RetentionPeriod.DAYS_30,
                        RetentionPeriod.DAYS_60
                    ).forEach { period ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = retentionPeriod == period,
                                onClick = {
                                    scope.launch {
                                        settings.setSetting(AppSettings.RETENTION_PERIOD, period.name)
                                        showRetentionDialog = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${period.days} days")
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = GalleryColors.SurfaceContainerHigh
        )
    }

    if (showGalleryOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showGalleryOptionsDialog = false },
            containerColor = Color(0xFF222328),
            shape = RoundedCornerShape(24.dp),
            title = { Text("Gallery Display", style = MaterialTheme.typography.titleLarge, color = GalleryColors.TextPrimary) },
            text = {
                Column {
                    Text(
                        "Grid Density",
                        style = MaterialTheme.typography.labelLarge,
                        color = GalleryColors.Primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val densityOptions: List<Pair<GridDensity, String>> = listOf(
                        GridDensity.COMFORTABLE to "Large (3 columns)",
                        GridDensity.NORMAL to "Medium (4 columns)",
                        GridDensity.COMPACT to "Small (5 columns)"
                    )
                    densityOptions.forEach { item ->
                        val density = item.first
                        val label = item.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        settings.setSetting(AppSettings.GRID_DENSITY, density.name)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = gridDensity == density,
                                onClick = {
                                    scope.launch {
                                        settings.setSetting(AppSettings.GRID_DENSITY, density.name)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GalleryColors.Primary,
                                    unselectedColor = GalleryColors.TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = GalleryColors.TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Default Sort Order",
                        style = MaterialTheme.typography.labelLarge,
                        color = GalleryColors.Primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val sortOptions: List<Pair<SortOrder, String>> = listOf(
                        SortOrder.DATE_DESC to "Date (Newest first)",
                        SortOrder.DATE_ASC to "Date (Oldest first)",
                        SortOrder.NAME_ASC to "Name (A–Z)",
                        SortOrder.SIZE_DESC to "Size (Largest first)"
                    )
                    sortOptions.forEach { item ->
                        val order = item.first
                        val label = item.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        settings.setSetting(AppSettings.SORT_ORDER, order.name)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortOrder == order,
                                onClick = {
                                    scope.launch {
                                        settings.setSetting(AppSettings.SORT_ORDER, order.name)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GalleryColors.Primary,
                                    unselectedColor = GalleryColors.TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = GalleryColors.TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGalleryOptionsDialog = false }) {
                    Text("Done", color = GalleryColors.Primary)
                }
            }
        )
    }
}
