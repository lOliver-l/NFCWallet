package com.nfcwallet.app

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.nfcwallet.app.data.local.NfcDatabase
import com.nfcwallet.app.data.repository.NfcCardRepository
import com.nfcwallet.app.nfc.reader.NfcReader
import com.nfcwallet.app.ui.NfcUiState
import com.nfcwallet.app.ui.home.MainScreen
import com.nfcwallet.app.ui.theme.NFCWalletTheme
import com.nfcwallet.app.viewmodel.NfcCardViewModel
import com.nfcwallet.app.viewmodel.NfcCardViewModelFactory

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcReader: NfcReader
    private lateinit var viewModel: NfcCardViewModel
    
    // Mutable state to drive the Compose UI
    private var nfcUiState by mutableStateOf<NfcUiState>(NfcUiState.Checking)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Safe initialization of Database, Repository and ViewModel inside onCreate
        val database = NfcDatabase.getDatabase(applicationContext)
        val repository = NfcCardRepository(database.nfcCardDao())
        viewModel = ViewModelProvider(this, NfcCardViewModelFactory(repository))[NfcCardViewModel::class.java]

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        // Initialize the NFC Reader with a callback to update UI
        nfcReader = NfcReader { cardInfo ->
            runOnUiThread {
                nfcUiState = NfcUiState.TagDetected(cardInfo)
            }
        }
        
        setContent {
            NFCWalletTheme {
                MainScreen(uiState = nfcUiState, viewModel = viewModel)
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
