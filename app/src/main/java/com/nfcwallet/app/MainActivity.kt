package com.nfcwallet.app

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.ui.theme.NFCWalletTheme

// State to hold NFC scanning status and data
sealed class NfcUiState {
    object Checking : NfcUiState()
    object NotSupported : NfcUiState()
    object Disabled : NfcUiState()
    object Waiting : NfcUiState()
    data class TagDetected(
        val uid: String,
        val techList: List<String>,
        val hasNfcA: Boolean,
        val atqa: String,
        val sak: String,
        val hasMifareClassic: Boolean,
        val mifareType: String,
        val size: Int,
        val sectorCount: Int,
        val blockCount: Int
    ) : NfcUiState()
}

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    // Mutable state to drive the Compose UI
    private var nfcUiState by mutableStateOf<NfcUiState>(NfcUiState.Checking)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        setContent {
            NFCWalletTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NfcScreen(
                        uiState = nfcUiState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter == null) {
            nfcUiState = NfcUiState.NotSupported
        } else if (nfcAdapter?.isEnabled == false) {
            nfcUiState = NfcUiState.Disabled
        } else {
            nfcUiState = NfcUiState.Waiting
            // Enable NFC Reader Mode to detect tags while app is active
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            val flags = NfcAdapter.FLAG_READER_NFC_A or 
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or 
                        NfcAdapter.FLAG_READER_NFC_V or 
                        NfcAdapter.FLAG_READER_NFC_BARCODE
            nfcAdapter?.enableReaderMode(this, this, flags, options)
        }
    }

    override fun onPause() {
        super.onPause()
        // Disable Reader Mode when app is in background
        nfcAdapter?.disableReaderMode(this)
    }

    // Called on a background thread when a tag is detected
    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return

        val uid = bytesToHex(tag.id)
        val techList = tag.techList.map { it.substringAfterLast('.') }
        
        val nfcA = NfcA.get(tag)
        val mifare = MifareClassic.get(tag)

        val newState = NfcUiState.TagDetected(
            uid = uid,
            techList = techList,
            hasNfcA = nfcA != null,
            atqa = nfcA?.atqa?.let { bytesToHex(it) } ?: "N/A",
            sak = nfcA?.sak?.let { "%02X".format(it) } ?: "N/A",
            hasMifareClassic = mifare != null,
            mifareType = mifare?.type?.let { getMifareTypeString(it) } ?: "N/A",
            size = mifare?.size ?: 0,
            sectorCount = mifare?.sectorCount ?: 0,
            blockCount = mifare?.blockCount ?: 0
        )

        // Update UI state on main thread
        runOnUiThread {
            nfcUiState = newState
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(":") { "%02X".format(it) }
    }

    private fun getMifareTypeString(type: Int): String {
        return when (type) {
            MifareClassic.TYPE_CLASSIC -> "Classic"
            MifareClassic.TYPE_PLUS -> "Plus"
            MifareClassic.TYPE_PRO -> "Pro"
            MifareClassic.TYPE_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
    }
}

@Composable
fun NfcScreen(uiState: NfcUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is NfcUiState.Checking -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Checking NFC status...")
            }
            is NfcUiState.NotSupported -> {
                Text(
                    text = "NFC is not supported on this device.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            is NfcUiState.Disabled -> {
                Text(
                    text = "NFC is disabled. Please enable it in your phone settings.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            is NfcUiState.Waiting -> {
                Text(
                    text = "Ready to scan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hold an NFC card near your phone",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is NfcUiState.TagDetected -> {
                TagInfoCard(uiState)
            }
        }
    }
}

@Composable
fun TagInfoCard(data: NfcUiState.TagDetected) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            item {
                Text("Tag Detected", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            item { InfoRow("UID", data.uid) }
            item { InfoRow("Technologies", data.techList.joinToString(", ")) }
            item { InfoRow("NfcA Available", data.hasNfcA.toString()) }
            if (data.hasNfcA) {
                item { InfoRow("ATQA", data.atqa) }
                item { InfoRow("SAK", data.sak) }
            }
            item { InfoRow("MifareClassic", data.hasMifareClassic.toString()) }
            if (data.hasMifareClassic) {
                item { InfoRow("MIFARE Type", data.mifareType) }
                item { InfoRow("Size", "${data.size} bytes") }
                item { InfoRow("Sectors", data.sectorCount.toString()) }
                item { InfoRow("Blocks", data.blockCount.toString()) }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text(text = value)
    }
}