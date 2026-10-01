package com.nfcwallet.app

import android.nfc.NfcAdapter
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
import com.nfcwallet.app.nfc.model.NfcCardInfo
import com.nfcwallet.app.nfc.reader.NfcReader
import com.nfcwallet.app.ui.theme.NFCWalletTheme

// State to hold NFC scanning status and data
sealed class NfcUiState {
    object Checking : NfcUiState()
    object NotSupported : NfcUiState()
    object Disabled : NfcUiState()
    object Waiting : NfcUiState()
    data class TagDetected(val cardInfo: NfcCardInfo) : NfcUiState()
}

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcReader: NfcReader
    
    // Mutable state to drive the Compose UI
    private var nfcUiState by mutableStateOf<NfcUiState>(NfcUiState.Checking)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        // Initialize the NFC Reader with a callback to update UI
        nfcReader = NfcReader { cardInfo ->
            runOnUiThread {
                nfcUiState = NfcUiState.TagDetected(cardInfo)
            }
        }
        
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
            
            nfcAdapter?.enableReaderMode(this, nfcReader, flags, options)
        }
    }

    override fun onPause() {
        super.onPause()
        // Disable Reader Mode when app is in background
        nfcAdapter?.disableReaderMode(this)
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
                TagInfoCard(uiState.cardInfo)
            }
        }
    }
}

@Composable
fun TagInfoCard(cardInfo: NfcCardInfo) {
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
            item { InfoRow("UID", cardInfo.uid) }
            item { InfoRow("Technologies", cardInfo.techList.joinToString(", ")) }
            item { InfoRow("NfcA Available", cardInfo.hasNfcA.toString()) }
            if (cardInfo.hasNfcA) {
                item { InfoRow("ATQA", cardInfo.atqa) }
                item { InfoRow("SAK", cardInfo.sak) }
            }
            item { InfoRow("MifareClassic", cardInfo.hasMifareClassic.toString()) }
            if (cardInfo.hasMifareClassic) {
                item { InfoRow("MIFARE Type", cardInfo.mifareType) }
                item { InfoRow("Size", "${cardInfo.size} bytes") }
                item { InfoRow("Sectors", cardInfo.sectorCount.toString()) }
                item { InfoRow("Blocks", cardInfo.blockCount.toString()) }
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