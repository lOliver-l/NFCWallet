package com.nfcwallet.app.nfc.model

// Expanded Data model representing comprehensive information read from any NFC tag
data class NfcCardInfo(
    val uid: String,
    val techList: List<String>,
    val hasNfcA: Boolean,
    val hasNfcB: Boolean,
    val hasNfcF: Boolean,
    val hasNfcV: Boolean,
    val isIsoDep: Boolean,
    val isNdef: Boolean,
    val ndefContent: String?,
    val atqa: String,
    val sak: String,
    val hasMifareClassic: Boolean,
    val hasMifareUltralight: Boolean,
    val mifareType: String,
    val size: Int,
    val sectorCount: Int,
    val blockCount: Int
)
