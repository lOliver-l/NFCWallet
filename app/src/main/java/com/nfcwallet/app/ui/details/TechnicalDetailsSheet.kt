package com.nfcwallet.app.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.nfc.model.NfcCardInfo
import com.nfcwallet.app.ui.components.CircularIconButton
import com.nfcwallet.app.ui.components.CopyableDataRow
import com.nfcwallet.app.ui.components.GlassSurface
import com.nfcwallet.app.ui.components.PillButton

// Glassmorphism bottom sheet desglosing expanded NFC standards
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalDetailsSheet(
    cardInfo: NfcCardInfo,
    isHideSensitiveInfo: Boolean = false,
    onDismiss: () -> Unit,
    onRequestAuth: (String, () -> Unit) -> Unit,
    onEditCustomization: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isRevealed by remember { mutableStateOf(!isHideSensitiveInfo) }
    var isTechnicalDetailsExpanded by remember { mutableStateOf(false) }

    fun maskUid(uid: String): String {
        val parts = uid.split(":")
        if (parts.size >= 4) {
            return "${parts.first()}:••:••:${parts.last()}"
        }
        return if (uid.length > 4) "${uid.take(2)}:••:${uid.takeLast(2)}" else "••:••"
    }

    val displayedUid = if (isRevealed) cardInfo.uid else maskUid(cardInfo.uid)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (cardInfo.isIsoDep) "IsoDep / EMV" else if (cardInfo.isNdef) "NDEF Tag" else "NFC",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Section 1: Card Information
            item {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularIconButton(
                                icon = Icons.Default.Info,
                                contentDescription = null,
                                onClick = {},
                                size = 32.dp,
                                iconSize = 16.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "IDENTIFICATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        CopyableDataRow("UID", displayedUid, isMonospace = true)

                        if (!isRevealed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            PillButton(
                                text = "Show Full UID",
                                onClick = {
                                    onRequestAuth("Authenticate to reveal full card details") {
                                        isRevealed = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (cardInfo.isNdef && !cardInfo.ndefContent.isNullOrBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            CopyableDataRow("NDEF Payload", cardInfo.ndefContent)
                        }
                    }
                }
            }

            // Section 2: Security & Protection Badge
            item {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularIconButton(
                                icon = Icons.Default.Lock,
                                contentDescription = null,
                                onClick = {},
                                size = 32.dp,
                                iconSize = 16.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SECURITY & PROTECTION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Encryption",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "AES-256 GCM (Keystore)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Biometric Lock",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Enabled",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Section 3: Collapsible Technical Details
            item {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTechnicalDetailsExpanded = !isTechnicalDetailsExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularIconButton(
                                    icon = Icons.Default.Star,
                                    contentDescription = null,
                                    onClick = { isTechnicalDetailsExpanded = !isTechnicalDetailsExpanded },
                                    size = 32.dp,
                                    iconSize = 16.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "TECHNICAL SPECIFICATIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (isTechnicalDetailsExpanded) "Hide" else "Show",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(
                            visible = isTechnicalDetailsExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                CopyableDataRow("Technologies", cardInfo.techList.joinToString(", "))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)

                                CopyableDataRow("NFC-A", if (cardInfo.hasNfcA) "Yes" else "No")
                                CopyableDataRow("NFC-B", if (cardInfo.hasNfcB) "Yes" else "No")
                                CopyableDataRow("NFC-F (FeliCa)", if (cardInfo.hasNfcF) "Yes" else "No")
                                CopyableDataRow("NFC-V", if (cardInfo.hasNfcV) "Yes" else "No")
                                CopyableDataRow("IsoDep (ISO 14443-4)", if (cardInfo.isIsoDep) "Yes" else "No")
                                CopyableDataRow("NDEF Tag", if (cardInfo.isNdef) "Yes" else "No")

                                if (cardInfo.hasNfcA) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                    CopyableDataRow("ATQA", cardInfo.atqa, isMonospace = true)
                                    CopyableDataRow("SAK", cardInfo.sak, isMonospace = true)
                                }

                                if (cardInfo.hasMifareClassic) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                    CopyableDataRow("MIFARE Classic", "Yes")
                                    CopyableDataRow("MIFARE Type", cardInfo.mifareType)
                                    CopyableDataRow("Sectors", cardInfo.sectorCount.toString())
                                    CopyableDataRow("Blocks", cardInfo.blockCount.toString())
                                }

                                if (cardInfo.hasMifareUltralight) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                    CopyableDataRow("MIFARE Ultralight", "Yes")
                                }

                                if (cardInfo.size > 0) {
                                    CopyableDataRow("Memory Size", "${cardInfo.size} bytes")
                                }
                            }
                        }
                    }
                }
            }

            // Customization & Delete Actions
            if (onEditCustomization != null) {
                item {
                    PillButton(
                        text = "Customize Card Style",
                        onClick = onEditCustomization,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                }
            }

            if (onDelete != null) {
                item {
                    OutlinedButton(
                        onClick = {
                            onRequestAuth("Authenticate to delete card") {
                                showDeleteConfirm = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete this saved card permanently?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(50)
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
