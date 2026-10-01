package com.nfcwallet.app.nfc.model

// Data model representing the basic public information read from an NFC tag
data class NfcCardInfo(
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
)
