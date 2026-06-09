package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.GradientBackground
import com.example.ui.components.GlassyCard
import com.example.ui.screens.*
import com.example.viewmodel.PhotoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: PhotoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                var currentTab by remember { mutableStateOf(0) }

                GradientBackground {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        bottomBar = {
                            PhotoVerseBottomBar(
                                selectedIndex = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    bottom = innerPadding.calculateBottomPadding()
                                )
                                .statusBarsPadding()
                        ) {
                            when (currentTab) {
                                0 -> PhotosScreen(
                                    viewModel = viewModel,
                                    onNavigateToStudio = { photo ->
                                        viewModel.setStudioPhoto(photo)
                                        currentTab = 3 // Route to Cloud Studio
                                    }
                                )
                                1 -> SearchScreen(
                                    viewModel = viewModel,
                                    onNavigateToTab = { currentTab = it }
                                )
                                2 -> AlbumsScreen(
                                    viewModel = viewModel,
                                    onNavigateToTab = { currentTab = it }
                                )
                                3 -> StudioCloudScreen(
                                    viewModel = viewModel
                                )
                                4 -> SettingsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom glassmorphic bottom navigation system
 */
@Composable
fun PhotoVerseBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val glassBg = if (isDark) {
        Color(0xFF0C101E).copy(alpha = 0.85f)
    } else {
        Color(0xFFF1F5F9).copy(alpha = 0.90f)
    }

    val borderColors = if (isDark) {
        Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.02f)))
    } else {
        Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.02f)))
    }

    Surface(
        color = glassBg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, borderColors, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(72.dp),
            tonalElevation = 0.dp
        ) {
            val tabs = listOf(
                Triple("Photos", Icons.Default.Collections, "tab_photos"),
                Triple("AI Search", Icons.Default.Search, "tab_search"),
                Triple("Vault", Icons.Default.FolderSpecial, "tab_albums"),
                Triple("Studio", Icons.Default.CloudSync, "tab_studio"),
                Triple("Settings", Icons.Default.Settings, "tab_settings")
            )

            tabs.forEachIndexed { index, (label, icon, tag) ->
                val isSelected = selectedIndex == index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.testTag(tag),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}
