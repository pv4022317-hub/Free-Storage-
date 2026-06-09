package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.api.GeminiApi
import com.example.ui.components.GlassyCard
import com.example.viewmodel.PhotoViewModel

@Composable
fun SettingsScreen(
    viewModel: PhotoViewModel,
    modifier: Modifier = Modifier
) {
    val currentPin by viewModel.vaultPIN.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isGeminiAvailable = viewModel.isGeminiAvailable

    val cachedMediaList by viewModel.cachedMediaList.collectAsState()
    val totalCachedBytes by viewModel.totalCachedBytes.collectAsState()

    // Form states
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var pinValueInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "App Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Configure biometric security, vaults, and Gemini credentials.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Section Card
        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "PV", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "PhotoVerse Pro Member",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "pv4022317@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Vault Setup
        Text(text = "Security Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        GlassyCard(
            modifier = Modifier.fillMaxWidth().testTag("security_settings_card"),
            cornerRadius = 16.dp,
            paddingValue = 12.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showPinChangeDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Change Vault Passcode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = "Current secure PIN: $currentPin", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Biometric Lock Icon", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Biometric Authentication", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = "Unlocks safe enclave using active face or dynamic touch ID sensor.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometricEnabled(it) },
                        modifier = Modifier.testTag("biometric_login_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Intelligent Gemini Integration Info Header
        Text(text = "Gemini Connectivity Hub", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))

        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (isGeminiAvailable) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isGeminiAvailable) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isGeminiAvailable) "Gemini Engine Connected" else "Direct Offline Mode fallback",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGeminiAvailable) "Full semantic image processing and multi-keyword search is completely active."
                        else "Gemini key placeholder is detected. To unlock multi-lingual translations and auto-tagging, enter your GEMINI_API_KEY inside AI Studio SECRETS panel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SQLite Storage Cache status
        Text(text = "Local SQLite Caching Engine", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        val cachedCount = remember(cachedMediaList) { cachedMediaList.size }
        val formattedCachedSize = remember(totalCachedBytes) {
            if (totalCachedBytes <= 0) "0 Bytes"
            else {
                val units = arrayOf("Bytes", "KB", "MB", "GB", "TB")
                val digitGroups = (Math.log10(totalCachedBytes.toDouble()) / Math.log10(1024.0)).toInt()
                String.format("%.2f %s", totalCachedBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
            }
        }

        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, paddingValue = 12.dp) {
            SpecificationRow(label = "Local Cache Store SQLite Status", value = "Operational")
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            SpecificationRow(label = "Indexed cached media records", value = "$cachedCount items")
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            SpecificationRow(label = "SQLite disk content space", value = formattedCachedSize)
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            SpecificationRow(label = "Caching optimization mode", value = "Write-Ahead Logging (WAL)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Credits / Specifications
        Text(text = "About PhotoVerse", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(8.dp))

        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, paddingValue = 12.dp) {
            SpecificationRow(label = "Platform Version", value = "PhotoVerse AI Cloud Pro v1.0.0")
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            SpecificationRow(label = "Architecture Plan", value = "Kotlin Compose + Local SQLite Room")
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            SpecificationRow(label = "Cloud Storage Limit", value = "21,990,232,555,520 Bytes (20 TB)")
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Change passcode dialog
    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Change Private Vault PIN") },
            text = {
                Column {
                    Text("Enter a new 4-digit code to protect hidden folders:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinValueInput,
                        onValueChange = { if (it.length <= 4) pinValueInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("pin_setup_input"),
                        singleLine = true,
                        placeholder = { Text("E.g., 5678") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinValueInput.length == 4) {
                            viewModel.updateVaultPIN(pinValueInput)
                            showPinChangeDialog = false
                            pinValueInput = ""
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}
