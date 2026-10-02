package com.nfcwallet.app

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.nfcwallet.app.data.local.NfcDatabase
import com.nfcwallet.app.data.repository.NfcCardRepository
import com.nfcwallet.app.nfc.reader.NfcReader
import com.nfcwallet.app.security.BiometricAuthManager
import com.nfcwallet.app.security.SecurityPreferences
import com.nfcwallet.app.ui.NfcUiState
import com.nfcwallet.app.ui.home.MainScreen
import com.nfcwallet.app.ui.lock.LockScreen
import com.nfcwallet.app.ui.theme.NFCWalletTheme
import com.nfcwallet.app.viewmodel.NfcCardViewModel
import com.nfcwallet.app.viewmodel.NfcCardViewModelFactory

class MainActivity : FragmentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcReader: NfcReader
    private lateinit var viewModel: NfcCardViewModel
    private lateinit var securityPreferences: SecurityPreferences
    private lateinit var biometricAuthManager: BiometricAuthManager

    private var nfcUiState by mutableStateOf<NfcUiState>(NfcUiState.Checking)
    private var isAppLocked by mutableStateOf(false)
    private var lockErrorMessage by mutableStateOf<String?>(null)
    private var currentThemeMode by mutableStateOf("system")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        securityPreferences = SecurityPreferences(this)
        biometricAuthManager = BiometricAuthManager(this)
        currentThemeMode = securityPreferences.themeMode

        val database = NfcDatabase.getDatabase(applicationContext)
        val repository = NfcCardRepository(database.nfcCardDao())
        viewModel = ViewModelProvider(this, NfcCardViewModelFactory(repository))[NfcCardViewModel::class.java]

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        nfcReader = NfcReader { cardInfo ->
            runOnUiThread {
                nfcUiState = NfcUiState.TagDetected(cardInfo)
            }
        }

        if (securityPreferences.isAuthRequired) {
            isAppLocked = true
        }

        setContent {
            NFCWalletTheme(themeMode = currentThemeMode) {
                if (isAppLocked) {
                    LockScreen(
                        errorMessage = lockErrorMessage,
                        onUnlockClick = { promptUnlock() }
                    )
                } else {
                    MainScreen(
                        uiState = nfcUiState,
                        viewModel = viewModel,
                        securityPreferences = securityPreferences,
                        biometricStatusMessage = biometricAuthManager.getBiometricStatusMessage(),
                        onRequestAuth = { reason, onSuccess ->
                            biometricAuthManager.authenticate(
                                title = "Authentication Required",
                                subtitle = reason,
                                onSuccess = onSuccess,
                                onError = { error ->
                                    lockErrorMessage = error
                                }
                            )
                        },
                        onThemeChanged = { newThemeMode ->
                            currentThemeMode = newThemeMode
                        }
                    )
                }
            }
        }
    }

    private fun promptUnlock() {
        lockErrorMessage = null
        biometricAuthManager.authenticate(
            title = "Unlock NFC Wallet",
            subtitle = "Your cards are protected",
            onSuccess = {
                isAppLocked = false
                lockErrorMessage = null
            },
            onError = { error ->
                lockErrorMessage = error
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter == null) {
            nfcUiState = NfcUiState.NotSupported
        } else if (nfcAdapter?.isEnabled == false) {
            nfcUiState = NfcUiState.Disabled
        } else {
            nfcUiState = NfcUiState.Waiting

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
        nfcAdapter?.disableReaderMode(this)
    }
}
