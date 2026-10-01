package com.nfcwallet.app.ui

import com.nfcwallet.app.nfc.model.NfcCardInfo

// State to hold NFC scanning status and data
sealed class NfcUiState {
    object Checking : NfcUiState()
    object NotSupported : NfcUiState()
    object Disabled : NfcUiState()
    object Waiting : NfcUiState()
    data class TagDetected(val cardInfo: NfcCardInfo) : NfcUiState()
}
