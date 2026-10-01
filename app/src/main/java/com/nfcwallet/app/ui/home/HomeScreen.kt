package com.nfcwallet.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.security.SecurityPreferences
import com.nfcwallet.app.ui.NfcUiState
import com.nfcwallet.app.ui.components.ScanningState
import com.nfcwallet.app.ui.components.WalletCard
import com.nfcwallet.app.ui.details.TechnicalDetailsSheet
import com.nfcwallet.app.ui.settings.SecuritySettingsTab
import com.nfcwallet.app.viewmodel.NfcCardViewModel

@Composable
fun MainScreen(
    uiState: NfcUiState,
    viewModel: NfcCardViewModel,
    securityPreferences: SecurityPreferences,
    biometricStatusMessage: String,
    onRequestAuth: (String, () -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Scan") },
                    label = { Text("Scan") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "My Cards") },
                    label = { Text("My Cards") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ScanTab(
                    uiState = uiState,
                    viewModel = viewModel,
                    isHideSensitiveInfo = securityPreferences.isHideSensitiveInfo,
                    onRequestAuth = onRequestAuth
                )
                1 -> MyCardsTab(
                    viewModel = viewModel,
                    isHideSensitiveInfo = securityPreferences.isHideSensitiveInfo,
                    onRequestAuth = onRequestAuth
                )
                2 -> SecuritySettingsTab(
                    securityPreferences = securityPreferences,
                    biometricStatusMessage = biometricStatusMessage,
                    onRequestAuthForToggle = { _, onResult ->
                        onRequestAuth("Authenticate to enable app lock") {
                            onResult(true)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTab(
    uiState: NfcUiState,
    viewModel: NfcCardViewModel,
    isHideSensitiveInfo: Boolean,
    onRequestAuth: (String, () -> Unit) -> Unit
) {
    var showTechnicalDetails by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "NFC Wallet", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is NfcUiState.Checking -> LoadingState("Checking NFC status...")
                is NfcUiState.NotSupported -> ErrorState("NFC is not supported on this device.")
                is NfcUiState.Disabled -> ErrorState("NFC is disabled. Please enable it in your phone settings.")
                is NfcUiState.Waiting -> ScanningState()
                is NfcUiState.TagDetected -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WalletCard(cardInfo = uiState.cardInfo)

                        Spacer(modifier = Modifier.height(32.dp))

                        InfoChipList(
                            technology = uiState.cardInfo.techList.firstOrNull() ?: "Unknown",
                            size = "${uiState.cardInfo.size}B",
                            sectors = uiState.cardInfo.sectorCount.toString(),
                            blocks = uiState.cardInfo.blockCount.toString()
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "Save card",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showTechnicalDetails = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "Technical details",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (showTechnicalDetails) {
                        TechnicalDetailsSheet(
                            cardInfo = uiState.cardInfo,
                            isHideSensitiveInfo = isHideSensitiveInfo,
                            onDismiss = { showTechnicalDetails = false },
                            onRequestAuth = onRequestAuth
                        )
                    }

                    if (showSaveDialog) {
                        var cardName by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showSaveDialog = false },
                            title = { Text("Save Card") },
                            text = {
                                OutlinedTextField(
                                    value = cardName,
                                    onValueChange = { cardName = it },
                                    label = { Text("Card Name (e.g. BLOC)") },
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (cardName.isNotBlank()) {
                                            viewModel.saveCard(uiState.cardInfo, cardName)
                                            showSaveDialog = false
                                        }
                                    }
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showSaveDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
fun InfoChipList(technology: String, size: String, sectors: String, blocks: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(label = "Technology", value = technology)
            InfoItem(label = "Size", value = size)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(label = "Sectors", value = sectors)
            InfoItem(label = "Blocks", value = blocks)
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
