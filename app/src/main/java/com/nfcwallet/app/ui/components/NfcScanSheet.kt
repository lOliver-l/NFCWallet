package com.nfcwallet.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.ui.NfcUiState
import com.nfcwallet.app.ui.details.TechnicalDetailsSheet

// Modal Popup Bottom Sheet for NFC Scanning
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScanSheet(
    uiState: NfcUiState,
    isHideSensitiveInfo: Boolean,
    onDismiss: () -> Unit,
    onSaveCardClick: () -> Unit,
    onRequestAuth: (String, () -> Unit) -> Unit
) {
    var showTechnicalDetails by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            when (uiState) {
                is NfcUiState.TagDetected -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WalletCard(
                            cardInfo = uiState.cardInfo,
                            isMasked = isHideSensitiveInfo
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        PillButton(
                            text = "Save Card to Wallet",
                            onClick = onSaveCardClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showTechnicalDetails = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Technical Specification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (showTechnicalDetails) {
                        TechnicalDetailsSheet(
                            cardInfo = uiState.cardInfo,
                            isHideSensitiveInfo = isHideSensitiveInfo,
                            onDismiss = { showTechnicalDetails = false },
                            onRequestAuth = onRequestAuth
                        )
                    }
                }
                else -> {
                    ScanningState()
                }
            }
        }
    }
}
