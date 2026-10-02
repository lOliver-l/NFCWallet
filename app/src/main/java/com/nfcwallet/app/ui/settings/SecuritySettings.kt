package com.nfcwallet.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.security.SecurityPreferences
import com.nfcwallet.app.ui.components.CircularIconButton
import com.nfcwallet.app.ui.components.RoundedSection
import com.nfcwallet.app.ui.components.WalletTopBar

// Google Wallet inspired Settings tab with Theme selection and back navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsTab(
    securityPreferences: SecurityPreferences,
    biometricStatusMessage: String,
    onRequestAuthForToggle: (Boolean, (Boolean) -> Unit) -> Unit,
    onThemeChanged: (String) -> Unit = {},
    onBackToCards: () -> Unit = {}
) {
    // Intercept system back press or back gesture
    BackHandler(onBack = onBackToCards)

    var isAuthRequired by remember { mutableStateOf(securityPreferences.isAuthRequired) }
    var isHideSensitiveInfo by remember { mutableStateOf(securityPreferences.isHideSensitiveInfo) }
    var currentThemeMode by remember { mutableStateOf(securityPreferences.themeMode) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            WalletTopBar(
                title = "Settings",
                onBackClick = onBackToCards
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Category 1: Security
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            RoundedSection {
                Column(modifier = Modifier.padding(18.dp)) {
                    SettingToggleItem(
                        icon = Icons.Default.Lock,
                        title = "Require authentication when opening app",
                        subtitle = "Locks the app with Biometrics or Device PIN/Pattern",
                        checked = isAuthRequired,
                        onCheckedChange = { targetState ->
                            if (targetState) {
                                onRequestAuthForToggle(true) { success ->
                                    if (success) {
                                        isAuthRequired = true
                                        securityPreferences.isAuthRequired = true
                                    }
                                }
                            } else {
                                isAuthRequired = false
                                securityPreferences.isAuthRequired = false
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SettingToggleItem(
                        icon = Icons.Default.Info,
                        title = "Hide sensitive card information",
                        subtitle = "Masks full UIDs and sensitive details until authenticated",
                        checked = isHideSensitiveInfo,
                        onCheckedChange = { targetState ->
                            isHideSensitiveInfo = targetState
                            securityPreferences.isHideSensitiveInfo = targetState
                        }
                    )
                }
            }

            // Category 2: Appearance & Theme Selection
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            RoundedSection {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularIconButton(
                            icon = Icons.Default.Star,
                            contentDescription = null,
                            onClick = {},
                            size = 40.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "App Theme Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Choose between White, Black, or System default",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme Chips: System, Light (White), Dark (Black)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentThemeMode == "system",
                            onClick = {
                                currentThemeMode = "system"
                                securityPreferences.themeMode = "system"
                                onThemeChanged("system")
                            },
                            label = { Text("System") }
                        )
                        FilterChip(
                            selected = currentThemeMode == "light",
                            onClick = {
                                currentThemeMode = "light"
                                securityPreferences.themeMode = "light"
                                onThemeChanged("light")
                            },
                            label = { Text("White (Light)") }
                        )
                        FilterChip(
                            selected = currentThemeMode == "dark",
                            onClick = {
                                currentThemeMode = "dark"
                                securityPreferences.themeMode = "dark"
                                onThemeChanged("dark")
                            },
                            label = { Text("Black (Dark)") }
                        )
                    }
                }
            }

            // Category 3: Data & Storage
            Text(
                text = "Data & Privacy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            RoundedSection {
                Column(modifier = Modifier.padding(18.dp)) {
                    SettingInfoItem(
                        icon = Icons.Default.Lock,
                        title = "Hardware Security Status",
                        subtitle = biometricStatusMessage
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingInfoItem(
                        icon = Icons.Default.Info,
                        title = "100% Offline Local Encryption",
                        subtitle = "AES-256 GCM using Android Keystore. No data leaves your device."
                    )
                }
            }

            // Category 4: About
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            RoundedSection {
                Column(modifier = Modifier.padding(18.dp)) {
                    SettingInfoItem(
                        icon = Icons.Default.Info,
                        title = "NFC Wallet v1.1",
                        subtitle = "Open Source • Privacy First • Native Android"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/lOliver-l/NFCWallet"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularIconButton(
                            icon = Icons.Default.Place,
                            contentDescription = null,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/lOliver-l/NFCWallet"))
                                context.startActivity(intent)
                            },
                            size = 40.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "GitHub Repository",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "https://github.com/lOliver-l/NFCWallet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularIconButton(
                icon = icon,
                contentDescription = null,
                onClick = {},
                size = 40.dp,
                iconSize = 20.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularIconButton(
            icon = icon,
            contentDescription = null,
            onClick = {},
            size = 40.dp,
            iconSize = 20.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
