package com.nfcwallet.app.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.nfc.model.NfcCardInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalDetailsSheet(
    cardInfo: NfcCardInfo,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp) // Extra padding for navigation bar
        ) {
            item {
                Text(
                    text = "Technical Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item { DetailRow("Full UID", cardInfo.uid) }
            item { DetailRow("Technologies", cardInfo.techList.joinToString(", ")) }
            item { DetailRow("NFC-A Available", if (cardInfo.hasNfcA) "Yes" else "No") }
            
            if (cardInfo.hasNfcA) {
                item { DetailRow("ATQA", cardInfo.atqa) }
                item { DetailRow("SAK", cardInfo.sak) }
            }
            
            item { DetailRow("MIFARE Classic", if (cardInfo.hasMifareClassic) "Yes" else "No") }
            
            if (cardInfo.hasMifareClassic) {
                item { DetailRow("MIFARE Type", cardInfo.mifareType) }
                item { DetailRow("Size", "${cardInfo.size} bytes") }
                item { DetailRow("Sector count", cardInfo.sectorCount.toString()) }
                item { DetailRow("Block count", cardInfo.blockCount.toString()) }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
