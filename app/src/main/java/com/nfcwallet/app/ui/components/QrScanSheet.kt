package com.nfcwallet.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.nfc.model.NfcCardInfo

// Sheet allowing user to scan or input a QR/Barcode pass into the wallet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanSheet(
    onDismiss: () -> Unit,
    onQrDetected: (NfcCardInfo) -> Unit
) {
    var qrContent by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularIconButton(
                icon = Icons.Default.Info,
                contentDescription = null,
                onClick = {},
                size = 64.dp,
                iconSize = 28.dp
            )

            Text(
                text = "Scan QR / Barcode Pass",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Enter or scan the digital QR code payload below",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = qrContent,
                onValueChange = { qrContent = it },
                label = { Text("QR Code Data / Serial Number") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            PillButton(
                text = "Add QR Pass to Wallet",
                onClick = {
                    if (qrContent.isNotBlank()) {
                        val cardInfo = NfcCardInfo(
                            uid = "QR:${qrContent.uppercase()}",
                            techList = listOf("QR Code Pass"),
                            hasNfcA = false,
                            hasNfcB = false,
                            hasNfcF = false,
                            hasNfcV = false,
                            isIsoDep = false,
                            isNdef = false,
                            ndefContent = null,
                            atqa = "N/A",
                            sak = "N/A",
                            hasMifareClassic = false,
                            hasMifareUltralight = false,
                            mifareType = "QR Pass",
                            size = qrContent.length,
                            sectorCount = 0,
                            blockCount = 0
                        )
                        onQrDetected(cardInfo)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = qrContent.isNotBlank()
            )
        }
    }
}
