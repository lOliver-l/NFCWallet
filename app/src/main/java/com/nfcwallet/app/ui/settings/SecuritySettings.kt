package com.nfcwallet.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.security.SecurityPreferences

// Settings screen tab allowing users to configure security and privacy preferences
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsTab(
    securityPreferences: SecurityPreferences,
    biometricStatusMessage: String,
    onRequestAuthForToggle: (Boolean, (Boolean) -> Unit) -> Unit
) {
    var isAuthRequired by remember { mutableStateOf(securityPreferences.isAuthRequired) }
    var isHideSensitiveInfo by remember { mutableStateOf(securityPreferences.isHideSensitiveInfo) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle: Require authentication on app startup
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Require authentication when opening the app",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = biometricStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAuthRequired,
                        onCheckedChange = { targetState ->
                            if (targetState) {
                                // Request authentication before enabling
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle: Hide sensitive card information by default
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hide sensitive card information",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Masks full UIDs and sensitive NFC details by default",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isHideSensitiveInfo,
                        onCheckedChange = { targetState ->
                            isHideSensitiveInfo = targetState
                            securityPreferences.isHideSensitiveInfo = targetState
                        }
                    )
                }
            }
        }
    }
}
