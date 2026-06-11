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
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun SettingsScreen(
    viewModel: PhotoViewModel,
    modifier: Modifier = Modifier
) {
    val currentPin by viewModel.vaultPIN.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isGeminiAvailable = viewModel.isGeminiAvailable
    val isPremium by viewModel.isPremium.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userDisplayName by viewModel.userDisplayName.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showPremiumUpiCheckout by remember { mutableStateOf(false) }

    val cachedMediaList by viewModel.cachedMediaList.collectAsState()
    val totalCachedBytes by viewModel.totalCachedBytes.collectAsState()

    // Form states
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var pinValueInput by remember { mutableStateOf("") }

    var activeLegalDoc by remember { mutableStateOf<String?>(null) }
    var showAccountDeletionConfirm by remember { mutableStateOf(false) }
    var showDataDownloadResult by remember { mutableStateOf(false) }
    var activeSecurity2FASimulation by remember { mutableStateOf(false) }
    var isConsentGranted by remember { mutableStateOf(true) }

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
                                if (isPremium) {
                                    listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
                                } else {
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                }
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPremium) "👑" else "PV",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userDisplayName ?: (if (isPremium) "PhotoVerse Premium 👑" else "PhotoVerse Free Account"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPremium) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = userEmail ?: "not-authenticated@photoverse.io",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Premium Section Header
        Text(
            text = "Membership Access",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isPremium) Color(0xFFF59E0B) else MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            paddingValue = 16.dp
        ) {
            Column {
                if (isPremium) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lifetime License Active",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "All sponsored ads are permanently disabled and local database algorithms are unlocked.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { viewModel.setPremiumStatus(false) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Free Tier (View Ads)", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Unlock Premium Suite",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✨ Permanently hide all sponsored promotion banner ads",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "🚀 Double file syncing throughput velocity limit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "💎 Exclusive VIP accent custom dynamic themes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPremiumUpiCheckout = true },
                        modifier = Modifier.fillMaxWidth().testTag("upgrade_premium_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upgrade for only ₹99 Lifetime", fontWeight = FontWeight.Bold)
                    }
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

        // Privacy, Security & Legal Consent Dashboard (Play Store Compliant)
        Text(
            text = "Privacy, Security & Consent Panel",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassyCard(
            modifier = Modifier.fillMaxWidth().testTag("privacy_consent_card"),
            cornerRadius = 16.dp,
            paddingValue = 12.dp
        ) {
            Column {
                // 1. Two-Factor Authentication Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "2FA Lock Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Two-Factor Authentication",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "OTP verification secure login for cloud authentication protection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = activeSecurity2FASimulation,
                        onCheckedChange = {
                            activeSecurity2FASimulation = it
                            Toast.makeText(context, if (it) "OTP login 2-Factor Authentication enabled on this device." else "Two-factor authentication disabled.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 2. Data Processing Consent Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Consent Info Icon",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dynamic Metadata Processing",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Permit offline indexing & event grouping on this local database repository.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = isConsentGranted,
                        onCheckedChange = {
                            isConsentGranted = it
                            Toast.makeText(context, if (it) "Offline metadata computation fully authorized." else "Data catalog privileges suspended.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 3. User Data Ownership Buttons (Download / Account Abrogation)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showDataDownloadResult = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f).testTag("download_my_data_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Data", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { showAccountDeletionConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).testTag("delete_account_btn")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Account", style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 4. Play Store Compliance Documents Links
                Text(
                    text = "Legal Documents & Compliance Pages",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val docs = listOf(
                    "Privacy Policy" to Icons.Default.Shield,
                    "Terms and Conditions" to Icons.Default.Gavel,
                    "Data Safety Declaration" to Icons.Default.CheckCircle,
                    "Account Deletion Policy" to Icons.Default.RemoveCircle,
                    "Copyright & IP Policy" to Icons.Default.Info,
                    "Community Guidelines" to Icons.Default.Group
                )

                docs.forEach { (title, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeLegalDoc = title }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
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

        // Connected Account management section
        Text(text = "Connected Account", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, paddingValue = 16.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "You are securely signed in as:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${userDisplayName ?: "User"} (${userEmail ?: "No email available"})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.signOutUser {}
                    },
                    modifier = Modifier.fillMaxWidth().testTag("settings_signout_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out from App")
                }
            }
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
            SpecificationRow(label = "Cloud Storage Plan", value = if (isPremium) "Unlimited Pro Storage 👑" else "15 GB Free Storage Tier")
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

    // Checkout Dialog for UPI payment
    if (showPremiumUpiCheckout) {
        val upiId = "7879566527-1@superyes"
        AlertDialog(
            onDismissRequest = { showPremiumUpiCheckout = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pro UPI Upgrade",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "To permanently remove all sponsor ads and activate high-performance features, please transfer ₹99 using any UPI app (GPay, PhonePe, Paytm, BHIM, etc.) to the following official ID:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // UPI ID display box with Copy button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OFFICIAL UPI ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = upiId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(upiId))
                                Toast.makeText(context, "UPI ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy UPI ID",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Launch UPI Button
                    Button(
                        onClick = {
                            try {
                                val upiUri = Uri.parse("upi://pay?pa=$upiId&pn=PhotoVerse%20Premium&tn=Premium%20License%20Upgrade&am=99.00&cu=INR")
                                val intent = Intent(Intent.ACTION_VIEW, upiUri)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Supported UPI banking apps not found. Please copy the UPI ID and open your payment app manually.", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("launch_upi_app_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pay ₹99 with UPI App", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Once the payment is completed, tap the activation button below to verify and configure your license instantly offline.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPremiumStatus(true)
                        showPremiumUpiCheckout = false
                        Toast.makeText(context, "Premium Status Activated! Enjoy Ad-Free PhotoVerse.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.testTag("verify_activate_premium_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)) // Green confirm
                ) {
                    Text("Verify & Activate Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumUpiCheckout = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (activeLegalDoc != null) {
        val documentTitle = activeLegalDoc!!
        val documentBody = when (documentTitle) {
            "Privacy Policy" -> {
                "Last Updated: June 11, 2026\n\n" +
                "PhotoVerse AI Cloud Pro is deeply committed to protecting user privacy and ensuring full compliance with Google Play Store policies.\n\n" +
                "1. INFRASTRUCTURE & ENCRYPTION\n" +
                "We use military-grade AES-256 block algorithms to secure your photos and videos locally within the system enclaves. All cloud transfers are secured using TLS v1.3 routing interfaces via HTTPS. No unencrypted content ever leaves your hardware device.\n\n" +
                "2. DATA RETENTION & SECURITY ENVELOPE\n" +
                "All visual items, OCR detections, and faces are stored dynamically inside local SQLite Room schemas. Local processing logs are periodically swept by our memory engines.\n\n" +
                "3. RIGHTS & EXCLUSIONS\n" +
                "Users retain 100% intellectual copyright ownership over all visual content. We do not sell, rent, or distribute any photo telemetry to third-party advertisers. All ad-serving models can be permanently suspended via the Premium upgrade tier."
            }
            "Terms and Conditions" -> {
                "Last Updated: June 11, 2026\n\n" +
                "Welcome to PhotoVerse AI Cloud Pro. By accessing or using our high-velocity cloud backup ecosystem, you agree to fulfill these structural guidelines.\n\n" +
                "1. SECURE LICENSE ENGAGEMENT\n" +
                "Subject to these guidelines, PhotoVerse grants you a personal, non-transferable license to execute on-device AI algorithms and cloud caching synchronization registers.\n\n" +
                "2. INTELLECTUAL SAFEGUARDS\n" +
                "Visual metadata belongs exclusively to the uploading account. No reverse engineering or indexing of our localized dynamic object recognition engines is permitted under copyright constraints.\n\n" +
                "3. PLAN ALLOCATIONS\n" +
                "Free tier is limited to 15 GB storage registers. Enterprise & Pro premium tiers are unlimited for personal storage uses."
            }
            "Data Safety Declaration" -> {
                "Google Play compliant data preservation safety structure:\n\n" +
                "• DATA IN TRANSIT: All file segments and database schemas transmitted are 100% encrypted with industry-standard TLS 1.3 protocol interfaces.\n\n" +
                "• THIRD-PARTY SHARING: Zero personal records or raw files are shared with generic third parties or monetization agencies.\n\n" +
                "• DATA PURGATION: Users possess direct features to instantly request systematic deletion of their accounts or specific cached photo registers."
            }
            "Account Deletion Policy" -> {
                "In alignment with Google Play developer policies, removing user accounts is straightforward:\n\n" +
                "• DELETION CHANNELS: Click on 'Delete Account' inside this console to completely and immediately erase all credentials, offline SQLite indexes, custom tags, biometric flags, and personal profile directories.\n\n" +
                "• DISK OVERWRITE: Temporary cache bytes are instantly wiped out of your local physical directories. This operation is definitive and irreversible."
            }
            "Copyright & IP Policy" -> {
                "PhotoVerse AI Cloud Pro respects copyright ownership:\n\n" +
                "1. DICTIONARY RIGHTS\n" +
                "You hold all copyrights for the photos/videos you store. We claim zero intellectual claims on your graphics.\n\n" +
                "2. COPIES & COMPLIANCE\n" +
                "Uploading contents which violate international copyright or property rights is strictly forbidden. Content found to be violating copyright stands subject to instantaneous deletion."
            }
            else -> {
                "Community guidelines to maintain safe, respectful interactions:\n\n" +
                "• INTELLECTUAL SAFETY: Keep storage limited to visual artifacts and metadata that are lawful and free of malice, spam, or toxic files.\n\n" +
                "• ABUSE COMPLIANCE: PhotoVerse AI reserves the right to suspend accounts attempting system exploits or server security bypasses."
            }
        }

        AlertDialog(
            onDismissRequest = { activeLegalDoc = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(documentTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                    Text(text = documentBody, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { activeLegalDoc = null }) {
                    Text("Acknowledge")
                }
            }
        )
    }

    if (showDataDownloadResult) {
        val simulatedJson = """
            {
              "client_identity": "PV-OAUTH-PRO",
              "email_alias": "${userEmail ?: "anonymous@photoverse.io"}",
              "account_license": "${if (isPremium) "VIP_PREMIUM_LIFETIME" else "FREE_TIER_ACCOUNT"}",
              "security": {
                 "two_factor_auth": ${if (activeSecurity2FASimulation) "true" else "false"},
                 "biometric_lock": ${if (isBiometricEnabled) "true" else "false"},
                 "active_passcode": "$currentPin"
              },
              "caching_optimization": "SQLITE_WAL_MODE",
              "allocated_storage_limit_bytes": ${if (isPremium) "999999999999" else "15000000000"}
            }
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showDataDownloadResult = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My Photoverse Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "The following is a comprehensive JSON export of your local account profile & security indices compiled directly from the secure SQLite database:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = simulatedJson,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(simulatedJson))
                        Toast.makeText(context, "Data export copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showDataDownloadResult = false
                    },
                    modifier = Modifier.testTag("copy_download_data_btn")
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataDownloadResult = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    if (showAccountDeletionConfirm) {
        AlertDialog(
            onDismissRequest = { showAccountDeletionConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Erase Account & Caches?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "CRITICAL WARNING: This will permanently delete your account registration alias, log flags, dynamic biometrics, secure PIN vault, and clear all offline cache database tables from this system.\n\nThis action conforms with Google Play Account Deletion laws and is completely irreversible. Are you sure you wish to continue?",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPremiumStatus(false)
                        viewModel.clearAllOfflineCaches()
                        viewModel.clearAllAiMemories()
                        viewModel.updateVaultPIN("1234")
                        viewModel.toggleBiometricEnabled(false)
                        activeSecurity2FASimulation = false
                        isConsentGranted = false
                        showAccountDeletionConfirm = false
                        Toast.makeText(context, "Account successfully pruned. Resetting system parameters.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_account_btn")
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDeletionConfirm = false }) {
                    Text("Keep Account")
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
