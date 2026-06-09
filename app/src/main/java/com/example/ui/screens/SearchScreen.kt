package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.database.PhotoEntity
import com.example.ui.components.GlassyCard
import com.example.viewmodel.PhotoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SearchScreen(
    viewModel: PhotoViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val activeFace by viewModel.activeFaceFilter.collectAsState()
    val activeTag by viewModel.activeTagFilter.collectAsState()
    val activeDate by viewModel.activeDateFilter.collectAsState()
    val activeLocation by viewModel.activeLocationFilter.collectAsState()
    val filteredPhotos by viewModel.filteredPublicPhotos.collectAsState()
    val allPhotosList by viewModel.publicPhotos.collectAsState()
    val isGeminiAvailable = viewModel.isGeminiAvailable

    // Extract dynamic locations and tags from SQLite database in real-time
    val dynamicLocations = remember(allPhotosList) {
        allPhotosList.map { it.location.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    val dynamicTags = remember(allPhotosList) {
        allPhotosList.flatMap { it.tags.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    var showFiltersPanel by remember { mutableStateOf(true) }
    var selectedSearchResultPhoto by remember { mutableStateOf<PhotoEntity?>(null) }

    val isAnyFilterActive = query.isNotBlank() || activeDate != "All" || activeLocation != null || activeTag != null || activeFace != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AI Smart Search",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Search people, objects, locations, or dates with Gemini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Responsive SQLite Offline Search Workbench Card
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_workbench_card"),
            cornerRadius = 24.dp,
            paddingValue = 16.dp
        ) {
            Column {
                // Inline Row with Search Field and Filters Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.updateQuery(it) },
                        placeholder = { Text("E.g., sunset with Sophia or mountains...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_field_input"),
                        shape = RoundedCornerShape(24.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear query")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showFiltersPanel = !showFiltersPanel },
                        modifier = Modifier
                            .background(
                                color = if (showFiltersPanel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                            .size(48.dp)
                            .testTag("toggle_filters_panel_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Toggle Search Filters",
                            tint = if (showFiltersPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Smooth Animation Expanding Real-time SQLite Filter Options
                AnimatedVisibility(
                    visible = showFiltersPanel,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                        // A. Real-time Date Filters Row
                        Text(
                            text = "Date Filter",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val dates = listOf("All", "Today", "Past Week", "Past Month", "Past Year")
                            dates.forEach { dateOpt ->
                                val selected = activeDate == dateOpt
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.selectDateFilter(dateOpt) },
                                    label = { Text(dateOpt, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // B. Real-time Location Filters Row
                        Text(
                            text = "Location Metadata (SQLite Dynamic)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (dynamicLocations.isEmpty()) {
                            Text(
                                text = "No locations discovered in database yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dynamicLocations.forEach { loc ->
                                    val selected = activeLocation?.lowercase() == loc.lowercase()
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.selectLocationFilter(loc) },
                                        label = { Text(loc, style = MaterialTheme.typography.labelMedium) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                            selectedLabelColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // C. Real-time Tags Filters Row
                        Text(
                            text = "Tag Metadata (SQLite Dynamic)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (dynamicTags.isEmpty()) {
                            Text(
                                text = "No catalogued tags found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dynamicTags.forEach { tagOpt ->
                                    val selected = activeTag?.lowercase() == tagOpt.lowercase()
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.selectTagFilter(tagOpt) },
                                        label = { Text(tagOpt, style = MaterialTheme.typography.labelMedium) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            selectedLabelColor = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search engine status alert banner
        if (isSearching) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.8.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini is translating context filters...",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isGeminiAvailable) Color(0xFF10B981) else Color(0xFFF59E0B),
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGeminiAvailable) "Gemini smart natural query active" else "Running standard keyword local scan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DYNAMICS ENGINE: Switch between Search Workbench Results VS Categories Navigation
        if (isAnyFilterActive) {
            // MATCHED RESULTS SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scan Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Found ${filteredPhotos.size} matching items from SQLite",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                TextButton(onClick = { viewModel.clearAllSearchFilters() }) {
                    Text("Clear All")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No results",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No photos matching the select database criteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                // 3-Column beautiful responsive search grid
                val rows = filteredPhotos.chunked(3)
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { selectedSearchResultPhoto = photo }
                            ) {
                                AsyncImage(
                                    model = photo.uri,
                                    contentDescription = photo.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Banner Overlay for location or date description
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(vertical = 2.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = photo.location.ifBlank { "Unlabelled" },
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        // Filler space
                        if (row.size < 3) {
                            for (i in 0 until (3 - row.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

        } else {
            // ORIGINAL STATIC DISCOVERY LANDING TILES
            // Section 1: Detected Face Recognition
            Text(
                text = "Detected Faces & People",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Biometric indexing offline group scanning.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Face Circle A
                FaceSelectCard(
                    name = "Sophia",
                    photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
                    isSelected = activeFace?.lowercase() == "sophia",
                    onClick = {
                        viewModel.selectFaceFilter("Sophia")
                    }
                )

                // Face Circle B
                FaceSelectCard(
                    name = "Rahul",
                    photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    isSelected = activeFace?.lowercase() == "rahul",
                    onClick = {
                        viewModel.selectFaceFilter("Rahul")
                    }
                )

                // Face Circle C
                FaceSelectCard(
                    name = "Emily",
                    photoUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                    isSelected = activeFace?.lowercase() == "emily",
                    onClick = {
                        viewModel.selectFaceFilter("Emily")
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Smart Categories
            Text(
                text = "AI Smart Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Grid cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmartCategoryCard(
                        title = "Mountains",
                        icon = Icons.Default.Terrain,
                        countLabel = "3 Items",
                        bannerUrl = "https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=400",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectTagFilter("mountain")
                        }
                    )
                    SmartCategoryCard(
                        title = "Portraits",
                        icon = Icons.Default.Face,
                        countLabel = "4 Items",
                        bannerUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectTagFilter("portrait")
                        }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmartCategoryCard(
                        title = "Festivals/Events",
                        icon = Icons.Default.Celebration,
                        countLabel = "3 Items",
                        bannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectTagFilter("event")
                        }
                    )
                    SmartCategoryCard(
                        title = "Landmarks",
                        icon = Icons.Default.LocationCity,
                        countLabel = "1 Item",
                        bannerUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=400",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectTagFilter("Paris")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Smart Objects Grouping
            Text(
                text = "Semantic Object Tagings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Offline tags extracted on upload.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            val objects = listOf("Food", "River", "Fireworks", "Balloons", "Abstract", "Lakes", "Cottages", "Street")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                objects.forEach { obj ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.selectTagFilter(obj)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = obj,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Photo Details / Explorer Overlay inside Search Screen
    selectedSearchResultPhoto?.let { photo ->
        Dialog(onDismissRequest = { selectedSearchResultPhoto = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = photo.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = photo.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = photo.location.ifBlank { "Unknown location" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    val dateFormatted = remember(photo.timestamp) {
                        val sdf = SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault())
                        sdf.format(Date(photo.timestamp))
                    }

                    Text(
                        text = "Captured: $dateFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tags pill lists
                    val tagsList = remember(photo.tags, photo.objects, photo.faces) {
                        (photo.tags.split(",") + photo.objects.split(",") + photo.faces.split(","))
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                    }

                    if (tagsList.isNotEmpty()) {
                        Text(text = "Keywords & Enriched Metadata", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tagsList.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Interactive Action Handles inside preview dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Favorite toggle
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleFavorite(photo)
                                selectedSearchResultPhoto = photo.copy(isFavorite = !photo.isFavorite)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite Toggle",
                                tint = if (photo.isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (photo.isFavorite) "Liked" else "Like", style = MaterialTheme.typography.labelSmall)
                        }

                        // Vault toggle
                        Button(
                            onClick = {
                                if (photo.isLockedInVault) {
                                    viewModel.unlockFromVault(photo)
                                    selectedSearchResultPhoto = photo.copy(isLockedInVault = false)
                                } else {
                                    viewModel.lockIntoVault(photo)
                                    selectedSearchResultPhoto = photo.copy(isLockedInVault = true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (photo.isLockedInVault) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (photo.isLockedInVault) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Vault Toggle"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (photo.isLockedInVault) "Unlock Public" else "Lock in Vault", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { selectedSearchResultPhoto = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss Preview", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FaceSelectCard(
    name: String,
    photoUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        val borderBrush = if (isSelected) {
            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
        } else {
            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .border(2.5.dp, borderBrush, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SmartCategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    countLabel: String,
    bannerUrl: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            AsyncImage(
                model = bannerUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.85f
            )

            // Transparent overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )

            // Foreground Text Details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Column {
                    Text(text = title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = countLabel, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
