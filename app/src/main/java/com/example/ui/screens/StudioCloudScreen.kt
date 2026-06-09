package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.database.PhotoEntity
import com.example.ui.components.GlassyCard
import com.example.ui.components.GlassyButton
import com.example.viewmodel.PhotoViewModel
import com.example.viewmodel.UploadStatus
import kotlinx.coroutines.delay

@Composable
fun StudioCloudScreen(
    viewModel: PhotoViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf("cloud") } // "cloud", "cleaner", "studio"

    val allPhotos by viewModel.allPhotos.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Custom Header switcher
        Text(
            text = "Cloud Studio",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Manage your 20 TB cloud backup and trigger AI touchups.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Premium Navigation Tabs for Cloud, Cleaner, Studio
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "cloud" -> 0
                "cleaner" -> 1
                "studio" -> 2
                else -> 0
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            Tab(
                selected = activeSubTab == "cloud",
                onClick = { activeSubTab = "cloud" },
                text = { Text("Backup Box") }
            )
            Tab(
                selected = activeSubTab == "cleaner",
                onClick = { activeSubTab = "cleaner" },
                text = { Text("Optimizer") }
            )
            Tab(
                selected = activeSubTab == "studio",
                onClick = { activeSubTab = "studio" },
                text = { Text("AI Studio") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Screen routing based on tabs
        when (activeSubTab) {
            "cloud" -> CloudBackupModule(viewModel = viewModel)
            "cleaner" -> StorageCleanerModule(viewModel = viewModel)
            "studio" -> AIEditingStudioModule(viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

/**
 * 20 TB storage status + synchronization triggers & detailed multi-threaded progress dashboard
 */
@Composable
fun CloudBackupModule(viewModel: PhotoViewModel) {
    val cloudFreeBytes by viewModel.cloudStorageFreeBytes.collectAsState()
    val cloudUsedBytes by viewModel.cloudStorageUsedBytes.collectAsState()
    val allPhotos by viewModel.allPhotos.collectAsState()
    val unbackedPhotos = remember(allPhotos) { allPhotos.filter { !it.isBackedUp } }

    val isSyncing by viewModel.isSyncing.collectAsState()
    val wifiOnly by viewModel.isWifiOnly.collectAsState()
    val speedTenX by viewModel.isTenXSpeedMode.collectAsState()
    val battSaver by viewModel.isBatterySaver.collectAsState()

    val uploadTasks by viewModel.uploadTasks.collectAsState()
    val isPaused by viewModel.isBackupPaused.collectAsState()
    val speedMbps by viewModel.uploadSpeedMbps.collectAsState()
    val activeThreads by viewModel.activeThreadsCount.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()

    // Space representations helper: GB scale
    val breakdown by viewModel.storageBreakdown.collectAsState()
    var selectedCategory by remember { mutableStateOf("all") } // "all", "Photos", "Videos", "Documents"
    var chartTab by remember { mutableStateOf("breakdown") } // "capacity" or "breakdown"

    val totalCapacityGB = 20480.0 // 20 TB
    val usedGB = remember(breakdown.usedBytes) { Math.round((breakdown.usedBytes / (1024.0 * 1024.0 * 1024.0)) * 100) / 100.0 }

    val photosGB = remember(breakdown.photosBytes) { Math.round((breakdown.photosBytes / (1024.0 * 1024.0 * 1024.0)) * 10) / 10.0 }
    val videosGB = remember(breakdown.videosBytes) { Math.round((breakdown.videosBytes / (1024.0 * 1024.0 * 1024.0)) * 10) / 10.0 }
    val documentsGB = remember(breakdown.documentsBytes) { Math.round((breakdown.documentsBytes / (1024.0 * 1024.0 * 1024.0)) * 10) / 10.0 }
    val freeGB = remember(breakdown.freeBytes) { Math.round((breakdown.freeBytes / (1024.0 * 1024.0 * 1024.0)) * 10) / 10.0 }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Storage statistics analytics
        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
            Text(
                text = "Cloud Storage Analytics Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Analyze your 20 TB space by file types and categories",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Tab Switcher (Segmented Button row style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("breakdown" to "File Categories", "capacity" to "Total Space Used").forEach { (tabKey, tabLabel) ->
                    val isSelected = chartTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { chartTab = tabKey }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Donut Chart container
            val slices = remember(chartTab, breakdown, photosGB, videosGB, documentsGB, freeGB) {
                if (chartTab == "capacity") {
                    listOf(
                        PieSliceData("Used Space", usedGB.toFloat(), Color(0xFF6366F1)),
                        PieSliceData("Free Space", freeGB.toFloat(), Color(0xFF94A3B8).copy(alpha = 0.25f))
                    )
                } else {
                    listOf(
                        PieSliceData("Photos", photosGB.toFloat(), Color(0xFF4F46E5)),
                        PieSliceData("Videos", videosGB.toFloat(), Color(0xFFF43F5E)),
                        PieSliceData("Documents", documentsGB.toFloat(), Color(0xFF10B981))
                    )
                }
            }

            val centerLabel = remember(chartTab, breakdown, usedGB) {
                if (chartTab == "capacity") {
                    String.format("%.2f%%", (usedGB / totalCapacityGB) * 100)
                } else {
                    String.format("%.1f GB", usedGB)
                }
            }

            val centerSubLabel = remember(chartTab) {
                if (chartTab == "capacity") "Used Capacity" else "Backed Up"
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StorageDonutChart(
                    slices = slices,
                    modifier = Modifier.size(170.dp),
                    thickness = 32f,
                    centerLabel = centerLabel,
                    centerSubLabel = centerSubLabel
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    slices.forEach { slice ->
                        val percent = if (usedGB > 0) {
                            val denominator = if (chartTab == "capacity") totalCapacityGB else usedGB
                            (slice.value / denominator) * 100
                        } else 0.0
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(slice.color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = slice.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (slice.name == "Free Space") {
                                        String.format("%.2f TB", slice.value / 1024.0)
                                    } else {
                                        String.format("%.1f GB", slice.value)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("(%.2f%%)", percent),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // File Type Interactive Categories (Photos, Videos, Documents)
            Text(
                text = "File Categories Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap a category card to inspect backed-up assets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoriesList = listOf(
                    Triple("Photos", Color(0xFF4F46E5), Icons.Default.Collections),
                    Triple("Videos", Color(0xFFF43F5E), Icons.Default.Videocam),
                    Triple("Documents", Color(0xFF10B981), Icons.Default.FolderSpecial)
                )

                categoriesList.forEach { (categoryName, categoryColor, categoryIcon) ->
                    val isSelected = selectedCategory == categoryName
                    val categorySizeValue = when (categoryName) {
                        "Photos" -> photosGB
                        "Videos" -> videosGB
                        "Documents" -> documentsGB
                        else -> 0.0
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) categoryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) categoryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedCategory = if (selectedCategory == categoryName) "all" else categoryName
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = categoryName,
                                tint = categoryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format("%.1f GB", categorySizeValue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = categoryColor
                            )
                        }
                    }
                }
            }

            // Interactive List of Backed-Up Source Database Items
            val filteredBackedUpList = remember(selectedCategory, allPhotos) {
                val backedUp = allPhotos.filter { it.isBackedUp }
                if (selectedCategory == "all") {
                    emptyList()
                } else {
                    backedUp.filter { photo ->
                        when (selectedCategory) {
                            "Videos" -> photo.isVideo
                            "Documents" -> {
                                photo.tags.lowercase().contains("document") || 
                                photo.tags.lowercase().contains("pdf") || 
                                photo.title.lowercase().endsWith(".pdf") || 
                                photo.title.lowercase().endsWith(".docx") || 
                                photo.title.lowercase().endsWith(".txt") ||
                                photo.title.lowercase().endsWith(".tiff") ||
                                photo.title.lowercase().contains("doc")
                            }
                            "Photos" -> {
                                !photo.isVideo && !(photo.tags.lowercase().contains("document") || 
                                photo.tags.lowercase().contains("pdf") || 
                                photo.title.lowercase().endsWith(".pdf") || 
                                photo.title.lowercase().endsWith(".docx") || 
                                photo.title.lowercase().endsWith(".txt") ||
                                photo.title.lowercase().endsWith(".tiff") ||
                                photo.title.lowercase().contains("doc"))
                            }
                            else -> true
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedCategory != "all",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Backed up $selectedCategory (${filteredBackedUpList.size} files)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Collapse",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { selectedCategory = "all" }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredBackedUpList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No live $selectedCategory backed up yet. Trigger a backup to sync files!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredBackedUpList.forEach { photo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Soft preview thumbnail or icon
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (photo.isVideo) {
                                            Icon(
                                                imageVector = Icons.Default.Videocam,
                                                contentDescription = null,
                                                tint = Color(0xFFF43F5E),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else if (photo.tags.lowercase().contains("document") || photo.title.lowercase().endsWith(".tiff")) {
                                            Icon(
                                                imageVector = Icons.Default.FolderSpecial,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            AsyncImage(
                                                model = photo.uri,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = photo.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${photo.location} • ${photo.fileSizeString}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Synced",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Backup Sync control card
        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Backup Queue Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (unbackedPhotos.isEmpty()) "Sabhi documents completely backed up!"
                            else "${unbackedPhotos.size} files ready for backup",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                // Quick Demo Insertion trigger for seamless testing
                IconButton(
                    onClick = { viewModel.injectDemoLargeUnbackedPhotos() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Demo Files",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master control buttons row
            if (isSyncing || uploadTasks.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isPaused) {
                                    viewModel.resumeWholeBackup()
                                } else {
                                    viewModel.pauseWholeBackup()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPaused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(23.dp)
                        ) {
                            Icon(
                                if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPaused) "Resume All" else "Pause All", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.cancelWholeBackup() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-thread connection dashboard details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Connections", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Text("$activeThreads Workers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Backup Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Text(String.format("%.2f Mbps", speedMbps), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.triggerPhotoBackupQueue() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("trigger_backup_all"),
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (unbackedPhotos.isEmpty()) "Sabhi Item Backup Hai!" else "Backup Queue Now (Multi-Threaded)",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (unbackedPhotos.isEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.injectDemoLargeUnbackedPhotos() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate 3 Large File Backups (RAW/Video)", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Connection queue details shown interactively
        if (uploadTasks.isNotEmpty()) {
            Text(
                text = "Backing Up Files...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Overlap visual progress bar
            GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${Math.round(syncProgress * 100)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { syncProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable details list of individual uploads
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uploadTasks.forEach { task ->
                        val taskProgress = if (task.totalBytes > 0) task.uploadedBytes.toFloat() / task.totalBytes else 0f
                        val taskProgressPercentage = Math.round(taskProgress * 100)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        when (task.status) {
                                            UploadStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.15f)
                                            UploadStatus.PAUSED -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                            UploadStatus.UPLOADING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (task.status) {
                                        UploadStatus.COMPLETED -> Icons.Default.Done
                                        UploadStatus.PAUSED -> Icons.Default.Pause
                                        UploadStatus.UPLOADING -> Icons.Default.Backup
                                        else -> Icons.Default.CloudQueue
                                    },
                                    contentDescription = null,
                                    tint = when (task.status) {
                                        UploadStatus.COMPLETED -> Color(0xFF10B981)
                                        UploadStatus.PAUSED -> Color(0xFFF59E0B)
                                        UploadStatus.UPLOADING -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details column
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$taskProgressPercentage%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Progress row
                                LinearProgressIndicator(
                                    progress = { taskProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when (task.status) {
                                        UploadStatus.COMPLETED -> Color(0xFF10B981)
                                        UploadStatus.PAUSED -> Color(0xFFF59E0B)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (task.status == UploadStatus.UPLOADING) {
                                            "${task.threadName} • ${String.format("%.1f", (task.speedBps / (1024f * 1024f)))} MB/s"
                                        } else {
                                            task.status.name
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", task.uploadedBytes / (1024f * 1024f))} / ${task.sizeString}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (task.status == UploadStatus.UPLOADING || task.status == UploadStatus.PAUSED) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.toggleTaskUploadPause(task.photoId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (task.status == UploadStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = "Toggle Pause",
                                        tint = if (task.status == UploadStatus.PAUSED) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Synchronization configuration settings parameters
        Text(
            text = "Upload Engine Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp, paddingValue = 12.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "WiFi Only Backup", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Conserve cellular mobile data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
                Switch(checked = wifiOnly, onCheckedChange = { viewModel.toggleWifiOnly(it) })
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "3 Concurrent Parallel Threads (10x Speed)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Simultaneous multi-threaded chunk uploads", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
                Switch(checked = speedTenX, onCheckedChange = { viewModel.toggleTenXSpeed(it) })
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Battery Saver Sync Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Syncs with reduced CPU utilization", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
                Switch(checked = battSaver, onCheckedChange = { viewModel.toggleBatterySaver(it) })
            }
        }
    }
}

/**
 * AI storage optimizer logic
 */
@Composable
fun StorageCleanerModule(viewModel: PhotoViewModel) {
    val similarPhotos by viewModel.similarPhotos.collectAsState()
    val reclaimableSpace by viewModel.reclaimableBytesString.collectAsState()
    var scanned by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }

    LaunchedEffect(scanning) {
        if (scanning) {
            delay(2000)
            scanning = false
            scanned = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassyCard(modifier = Modifier.fillMaxWidth().testTag("checksum_analyzer_card"), cornerRadius = 24.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Checksum Duplicate Analyzer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Compares SHA-256 file checksum hashes in the local database to locate exact redundant duplicate elements and free up media storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (scanning) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Analyzing SQLite database hashes...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                } else if (!scanned) {
                    GlassyButton(
                        text = "Scan Database Hashes",
                        onClick = { scanning = true },
                        modifier = Modifier.fillMaxWidth().testTag("scan_hashes_button")
                    )
                } else {
                    if (similarPhotos.isEmpty()) {
                        Icon(Icons.Default.Done, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Storage fully optimized! No clutter found.",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { scanned = false },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Rescan") }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            // Reclaimable metric badge
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = reclaimableSpace,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Wasted Space Reclaimable",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Found ${similarPhotos.size} Redundant File Duplicates:",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Individual duplicate details list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                similarPhotos.forEach { photo ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = photo.uri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(10.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = photo.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "Size: ${photo.fileSizeString} • ${photo.location}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                            // Render Hash key badge
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "HASH: ${photo.fileHash}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.cleanSimilarPhotos() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("delete_duplicates_button"),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete Duplicates", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * AI Editing studio layout
 */
@Composable
fun AIEditingStudioModule(viewModel: PhotoViewModel) {
    val selectedPhoto by viewModel.selectedStudioPhoto.collectAsState()
    val allPhotos by viewModel.allPhotos.collectAsState()

    val loadingText by viewModel.studioLoadingText.collectAsState()
    val editedUri by viewModel.studioEditedUri.collectAsState()
    val activeEffect by viewModel.activeEditingEffect.collectAsState()

    // Slider comparison position: 0 to 1f
    var sliderRatio by remember { mutableStateOf(0.5f) }

    if (selectedPhoto == null) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Outlined.AutoFixHigh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "AI Editing Studio Client", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Pick a photo to apply professional single-click AI modifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(text = "Choose Photo to edit:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            // Grid of choices
            val selectablePhotos = remember(allPhotos) { allPhotos.filter { !it.isVideo } }
            if (selectablePhotos.isEmpty()) {
                Text("Selectable images not loaded.")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectablePhotos.forEach { photo ->
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setStudioPhoto(photo) }
                        ) {
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    } else {
        val photo = selectedPhoto!!

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.setStudioPhoto(null) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    Text("Choose Another")
                }
                Text(text = "Active: ${photo.title}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            // Visual comparative preview box
            Box(
                modifier = Modifier
                    .fill開Width()
                    .height(263.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (loadingText != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = loadingText!!, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                } else if (editedUri != null) {
                    // Split comparison view using SliderRatio (or side-by-side)
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Original Left side
                        Box(
                            modifier = Modifier
                                .weight(sliderRatio)
                                .fillMaxHeight()
                        ) {
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                "Original",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(0.6f))
                                    .padding(horizontal = 4.dp)
                            )
                        }
                        
                        Divider(modifier = Modifier.fillMaxHeight().width(2.dp), color = MaterialTheme.colorScheme.primary)

                        // Edited Right side
                        Box(
                            modifier = Modifier
                                .weight(1f - sliderRatio)
                                .fillMaxHeight()
                        ) {
                            AsyncImage(
                                model = editedUri!!,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                "AI Enhanced",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(0.6f))
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // If edited, show slider control to compare!
            if (editedUri != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Slide to Compare (Befor / After)",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Slider(
                        value = sliderRatio,
                        onValueChange = { sliderRatio = it },
                        valueRange = 0.1f..0.9f
                    )
                }
            }

            // Toolkit parameters
            Text(text = "Choose AI Filter Action:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

            val actions = listOf(
                Pair("remove_bg", "Erase BG"),
                Pair("remove_obj", "Clean Objects"),
                Pair("retouch", "AI Retouch"),
                Pair("upscale", "4K Super-Resolution")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actions.chunked(2).forEach { chunk ->
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunk.forEach { item ->
                            val isSelected = activeEffect == item.first
                            Button(
                                onClick = { viewModel.applyStudioEditingEffect(item.first) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = item.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extension to avoid import issues on compile scope
private fun Modifier.fill開Width(): Modifier = this.fillMaxWidth()

data class PieSliceData(
    val name: String,
    val value: Float,
    val color: Color
)

@Composable
fun StorageDonutChart(
    slices: List<PieSliceData>,
    modifier: Modifier = Modifier,
    thickness: Float = 36f,
    centerLabel: String = "",
    centerSubLabel: String = ""
) {
    val animateProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(slices) {
        animateProgress.snapTo(0f)
        animateProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 800,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = slices.sumOf { it.value.toDouble() }.toFloat()
            val currProgress = animateProgress.value
            if (total > 0f) {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = (slice.value / total) * 360f
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle * currProgress,
                            useCenter = false,
                            style = Stroke(width = thickness, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweepAngle
                }
            } else {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = thickness, cap = StrokeCap.Round)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (centerSubLabel.isNotEmpty()) {
                Text(
                    text = centerSubLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

