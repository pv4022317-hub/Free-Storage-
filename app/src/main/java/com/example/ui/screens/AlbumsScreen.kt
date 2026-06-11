package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.database.PhotoEntity
import com.example.data.database.AiMemoryAlbumEntity
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.GlassyCard
import com.example.ui.components.GlassyButton
import com.example.viewmodel.PhotoViewModel
import androidx.compose.ui.window.Dialog

@Composable
fun AlbumsScreen(
    viewModel: PhotoViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubView by remember { mutableStateOf("albums") } // "albums", "favorites", "vault", "videos", "ai_album"
    
    // AI Memories Data & Controller States
    val aiMemories by viewModel.aiMemoriesList.collectAsState()
    val isServiceRunning by viewModel.isAiMemoriesServiceRunning.collectAsState()
    val scanInterval by viewModel.aiMemoriesScanIntervalSeconds.collectAsState()
    val lastScanTime by viewModel.lastAiMemoriesScanTimestamp.collectAsState()
    val isScanning by viewModel.isScannerCurrentlyScanning.collectAsState()
    val scanLogs by viewModel.aiMemoriesScanLogs.collectAsState()

    var selectedAiMemory by remember { mutableStateOf<AiMemoryAlbumEntity?>(null) }

    // Vault PIN Entry state
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val vaultPhotos by viewModel.vaultPhotos.collectAsState()
    val favorites by viewModel.favoritePhotos.collectAsState()
    val allPhotos by viewModel.allPhotos.collectAsState()

    val videos = remember(allPhotos) { allPhotos.filter { it.isVideo } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Breadcrumb back navigation if in subview
        if (activeSubView != "albums") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeSubView = "albums" }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = when (activeSubView) {
                        "favorites" -> "Starred Favorites"
                        "videos" -> "High Dynamic Videos"
                        "vault" -> "Secure Private Vault"
                        "ai_album" -> selectedAiMemory?.title ?: "AI Memory Highlights"
                        else -> "Album Collections"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "My Albums",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Explore smart categorised photo & video folders.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sub View Routing
        when (activeSubView) {
            "albums" -> {
                // Horizontal / Categories Grid
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AlbumFolderCard(
                            title = "Favorites",
                            count = "${favorites.size} Items",
                            coverUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300",
                            icon = Icons.Default.Favorite,
                            badgeColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = { activeSubView = "favorites" }
                        )

                        AlbumFolderCard(
                            title = "Videos",
                            count = "${videos.size} Clips",
                            coverUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=300",
                            icon = Icons.Default.Videocam,
                            badgeColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { activeSubView = "videos" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AlbumFolderCard(
                            title = "Private Vault",
                            count = if (isVaultUnlocked) "${vaultPhotos.size} Locked" else "Encrypted PIN",
                            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=300",
                            icon = Icons.Default.Lock,
                            badgeColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = { activeSubView = "vault" }
                        )

                        AlbumFolderCard(
                            title = "Local Device",
                            count = "${allPhotos.size} Total",
                            coverUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300",
                            icon = Icons.Default.PhoneAndroid,
                            badgeColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToTab(0) }
                        )
                    }
                }

                // --- AI Auto-Generated Smart Albums Section ---
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Smart Memories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "SQLite-heuristics auto-grouped memory events.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (aiMemories.isEmpty()) {
                    GlassyCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        cornerRadius = 16.dp,
                        paddingValue = 16.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Scanning",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp).animateContentSize()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Analyzing SQLite photo metadata...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "The periodic background service analyzes location proximity, system date offsets and event tags to group your story.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    // Horizontal scrollable list (carousel) of AI Smart Memory card folders!
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        items(aiMemories) { memory ->
                            val photoIds = remember(memory.photoIdsString) {
                                memory.photoIdsString.split(",").mapNotNull { it.toIntOrNull() }
                            }
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(200.dp)
                                    .clickable {
                                        selectedAiMemory = memory
                                        activeSubView = "ai_album"
                                    },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = memory.coverPhotoUri,
                                        contentDescription = memory.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                                )
                                            )
                                    )
                                    // Top left badge for memory type
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp)
                                            .background(
                                                when (memory.memoryType) {
                                                    "LOCATION" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                                    "DATE" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                                                    else -> Color(0xFFEC4899).copy(alpha = 0.85f)
                                                },
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = memory.memoryType,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Info details at the bottom of the memory item
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = memory.title,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${photoIds.size} Items",
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- SECTION: AI Engine Service Controller & Logs Terminal ---
                Spacer(modifier = Modifier.height(16.dp))
                var showMonitorPanel by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMonitorPanel = !showMonitorPanel }
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isServiceRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = if (isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SQLite AI Memory Daemon",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isServiceRunning) "Status: Periodically clustering (${scanInterval}s)" else "Status: Paused / Standby Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (isServiceRunning) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (showMonitorPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showMonitorPanel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassyCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        cornerRadius = 16.dp,
                        paddingValue = 12.dp
                    ) {
                        Column {
                            // Control Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.toggleAiMemoriesService() },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isServiceRunning) MaterialTheme.colorScheme.error.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isServiceRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isServiceRunning) "Pause Loop" else "Resume Loop",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { viewModel.triggerManualAiMemoriesScan() },
                                    modifier = Modifier.weight(1.5f),
                                    enabled = !isScanning,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Force Scan Now", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scan Interval:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(5, 15, 30, 60).forEach { sec ->
                                        val selected = scanInterval == sec
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable { viewModel.changeScanInterval(sec) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${sec}s",
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total AI Clusters in local SQL:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(
                                    onClick = { viewModel.clearAllAiMemories() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Flush SQLite Albums", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Live SQLite Parser Thread Logs",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Monospaced simulated scrolling Console logs
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(scanLogs) { log ->
                                        Text(
                                            text = log,
                                            color = when {
                                                log.contains("SUCCESS", ignoreCase = true) || log.contains("HIT", ignoreCase = true) -> Color(0xFF10B981)
                                                log.contains("ERROR", ignoreCase = true) || log.contains("CRITICAL", ignoreCase = true) -> Color(0xFFEF4444)
                                                log.contains("PROXIMITY", ignoreCase = true) || log.contains("TEMPORAL", ignoreCase = true) || log.contains("THEMATIC", ignoreCase = true) -> Color(0xFF3B82F6)
                                                log.contains("query", ignoreCase = true) -> Color(0xFFFABF2C)
                                                else -> Color(0xFFB3B3B3)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "favorites" -> {
                if (favorites.isEmpty()) {
                    EmptyAlbumMessage(msg = "Koi favorite item nahi hai! Tab star kijiye.")
                } else {
                    AlbumPhotosGrid(photos = favorites, onUnlock = null)
                }
            }

            "videos" -> {
                if (videos.isEmpty()) {
                    EmptyAlbumMessage(msg = "Feed me koi video content nahi mila!")
                } else {
                    AlbumPhotosGrid(photos = videos, onUnlock = null)
                }
            }

            "vault" -> {
                if (!isVaultUnlocked) {
                    VaultEntryGate(viewModel = viewModel)
                } else {
                    VaultPrivateGallery(
                        vaultPhotos = vaultPhotos,
                        viewModel = viewModel,
                        onLockVaultClick = { viewModel.lockVault() }
                    )
                }
            }

            "ai_album" -> {
                selectedAiMemory?.let { memory ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = memory.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pattern Criteria: " + memory.triggerPattern,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val aiAlbumPhotos = remember(memory, allPhotos) {
                            val idSet = memory.photoIdsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                            allPhotos.filter { it.id in idSet }
                        }

                        if (aiAlbumPhotos.isEmpty()) {
                            EmptyAlbumMessage(msg = "Offline media items within this cluster couldn't be loaded.")
                        } else {
                            AlbumPhotosGrid(photos = aiAlbumPhotos, onUnlock = null)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun AlbumFolderCard(
    title: String,
    count: String,
    coverUrl: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Album Cover
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic vignette background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Top Left Action Icon Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(badgeColor, CircleShape)
                    .padding(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }

            // Bottom Folder Title Details
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(text = title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = count, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * PIN-pad lock screen to gate Vault access with biometric verify scanner simulation
 */
@Composable
fun VaultEntryGate(viewModel: PhotoViewModel) {
    var pinValue by remember { mutableStateOf("") }
    val errorMsg by viewModel.vaultError.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    var showBiometricPrompt by remember { mutableStateOf(isBiometricEnabled) }

    // Biometric scanner dialogue prompt overlay
    if (showBiometricPrompt && isBiometricEnabled) {
        var scanState by remember { mutableStateOf("READY") } // "READY", "SCANNING", "SUCCESS"

        LaunchedEffect(scanState) {
            if (scanState == "SCANNING") {
                kotlinx.coroutines.delay(1200)
                scanState = "SUCCESS"
            } else if (scanState == "SUCCESS") {
                kotlinx.coroutines.delay(600)
                viewModel.authenticateVaultWithBiometric()
                showBiometricPrompt = false
            }
        }

        AlertDialog(
            onDismissRequest = {
                showBiometricPrompt = false
                scanState = "READY"
            },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Fingerprint Touch Area containing glowing rings
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                when (scanState) {
                                    "SUCCESS" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    "SCANNING" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                },
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                when (scanState) {
                                    "SUCCESS" -> Color(0xFF10B981).copy(alpha = 0.5f)
                                    "SCANNING" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                },
                                CircleShape
                            )
                            .clickable(enabled = scanState == "READY") {
                                scanState = "SCANNING"
                            }
                            .testTag("fingerprint_scan_sensor"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Scan icon",
                            tint = when (scanState) {
                                "SUCCESS" -> Color(0xFF10B981)
                                "SCANNING" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = when (scanState) {
                            "SCANNING" -> "Scanning Biometric Signature..."
                            "SUCCESS" -> "Touch ID Match Successful!"
                            else -> "Confirm Touch or Face ID"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (scanState) {
                            "SCANNING" -> "Verifying hardware sensor nodes... keep your finger steady."
                            "SUCCESS" -> "Unlocking AES-256 local SQL cipher cluster..."
                            else -> "Tap the fingerprint sensor above to scan the biometrics of the active device."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    if (scanState == "SCANNING") {
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(0.75f).clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showBiometricPrompt = false
                        scanState = "READY"
                    },
                    modifier = Modifier.testTag("cancel_biometric_btn").fillMaxWidth()
                ) {
                    Text("Use 4-Digit Passcode PIN Instead", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
        )
    }

    GlassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .animateContentSize(),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Secure Private Enclave",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Double-Secured via SHA-256 local file masking indexes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display PIN placeholder dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < pinValue.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }

            // Quick fingerprint trigger shortcut if biometric enrollment is active
            if (isBiometricEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                IconButton(
                    onClick = { showBiometricPrompt = true },
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), CircleShape)
                        .testTag("trigger_biometric_gate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Trigger Touch ID Authentication Scan",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "Verify using Biometric ID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Error Message
            errorMsg?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful Numeric PIN keyboard layout
            val buttons = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "OK")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { entry ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.8f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (entry == "OK") Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)))
                                    )
                                    .clickable {
                                        when (entry) {
                                            "Clear" -> {
                                                if (pinValue.isNotEmpty()) pinValue = pinValue.dropLast(1)
                                            }
                                            "OK" -> {
                                                if (pinValue.length == 4) {
                                                    viewModel.authenticateVault(pinValue)
                                                }
                                            }
                                            else -> {
                                                if (pinValue.length < 4) {
                                                    pinValue += entry
                                                    // Auto trigger OK check if 4 digits
                                                    if (pinValue.length == 4) {
                                                        viewModel.authenticateVault(pinValue)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("pin_btn_$entry"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = entry,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (entry == "OK") Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Hint: Default secure password is \"1234\"",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Dynamic Vault photos overview list when unlocked
 */
@Composable
fun VaultPrivateGallery(
    vaultPhotos: List<PhotoEntity>,
    viewModel: PhotoViewModel,
    onLockVaultClick: () -> Unit
) {
    var photoToDelete by remember { mutableStateOf<PhotoEntity?>(null) }
    var selectedPreviewPhoto by remember { mutableStateOf<PhotoEntity?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Highly Encrypted Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = onLockVaultClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lock Vault")
            }
        }

        if (vaultPhotos.isEmpty()) {
            EmptyAlbumMessage(msg = "Private vault me koi items locked nahi hain! Photos tab me kisi item ko select kar ke lock kijiye.")
        } else {
            AlbumPhotosGrid(
                photos = vaultPhotos,
                onUnlock = { photo -> viewModel.unlockFromVault(photo) },
                onDelete = { photo -> photoToDelete = photo },
                onPhotoClick = { photo -> selectedPreviewPhoto = photo }
            )
        }
    }

    // Detail Preview Dialog for Vault Photos
    if (selectedPreviewPhoto != null) {
        val currentPhoto = selectedPreviewPhoto!!
        Dialog(onDismissRequest = { selectedPreviewPhoto = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentPhoto.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentPhoto.location.isNotBlank()) {
                                Text(
                                    text = currentPhoto.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        IconButton(onClick = { selectedPreviewPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = currentPhoto.uri,
                            contentDescription = currentPhoto.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.unlockFromVault(currentPhoto)
                                selectedPreviewPhoto = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unlock")
                        }

                        Button(
                            onClick = {
                                photoToDelete = currentPhoto
                                selectedPreviewPhoto = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog
    if (photoToDelete != null) {
        val deletingPhoto = photoToDelete!!
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Permanently delete image?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${deletingPhoto.title}\" from the local storage vault? This action is irreversible, and the image data will be lost forever.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhoto(deletingPhoto)
                        photoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Generic grid displayed inside sub album paths
 */
@Composable
fun AlbumPhotosGrid(
    photos: List<PhotoEntity>,
    onUnlock: ((PhotoEntity) -> Unit)? = null,
    onDelete: ((PhotoEntity) -> Unit)? = null,
    onPhotoClick: ((PhotoEntity) -> Unit)? = null
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        val rows = photos.chunked(3)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { photo ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.1f))
                            .then(
                                if (onPhotoClick != null) {
                                    Modifier.clickable { onPhotoClick(photo) }
                                } else Modifier
                            )
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Option overlay to unlock vault directly
                        if (onUnlock != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .clickable { onUnlock(photo) }
                                    .padding(6.dp)
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = "Unlock", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Option overlay to delete vault photo permanently
                        if (onDelete != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .clickable { onDelete(photo) }
                                    .padding(6.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
                if (row.size < 3) {
                    for (i in 0 until (3 - row.size)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAlbumMessage(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = msg,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
