package com.nfcwallet.app.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.nfc.model.NfcCardInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalDetailsSheet(
    cardInfo: NfcCardInfo,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
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

            if (onDelete != null) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Card")
                    }
                }
            }
        }
    }
    
    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete this saved card?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
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
