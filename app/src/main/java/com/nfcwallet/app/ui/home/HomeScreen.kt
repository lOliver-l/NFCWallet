package com.nfcwallet.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.ui.NfcUiState
import com.nfcwallet.app.ui.components.ScanningState
import com.nfcwallet.app.ui.components.WalletCard
import com.nfcwallet.app.ui.details.TechnicalDetailsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(uiState: NfcUiState) {
    var showTechnicalDetails by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "NFC Wallet",
                        fontWeight = FontWeight.Bold
                    )
                },
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
                is NfcUiState.Checking -> {
                    LoadingState("Checking NFC status...")
                }
                is NfcUiState.NotSupported -> {
                    ErrorState("NFC is not supported on this device.")
                }
                is NfcUiState.Disabled -> {
                    ErrorState("NFC is disabled. Please enable it in your phone settings.")
                }
                is NfcUiState.Waiting -> {
                    ScanningState()
                }
                is NfcUiState.TagDetected -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WalletCard(cardInfo = uiState.cardInfo)
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Basic info below the card
                        InfoChipList(
                            technology = uiState.cardInfo.techList.firstOrNull() ?: "Unknown",
                            size = "${uiState.cardInfo.size}B",
                            sectors = uiState.cardInfo.sectorCount.toString(),
                            blocks = uiState.cardInfo.blockCount.toString()
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { showTechnicalDetails = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
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
                            onDismiss = { showTechnicalDetails = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(message: String) {
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
private fun ErrorState(message: String) {
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
private fun InfoChipList(technology: String, size: String, sectors: String, blocks: String) {
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
private fun InfoItem(label: String, value: String) {
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
