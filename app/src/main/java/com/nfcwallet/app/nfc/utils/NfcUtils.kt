package com.nfcwallet.app.nfc.utils

import android.nfc.tech.MifareClassic

// Utility functions for NFC data conversion and formatting
object NfcUtils {
    
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(":") { "%02X".format(it) }
    }

    fun getMifareTypeString(type: Int): String {
        return when (type) {
            MifareClassic.TYPE_CLASSIC -> "Classic"
            MifareClassic.TYPE_PLUS -> "Plus"
            MifareClassic.TYPE_PRO -> "Pro"
            MifareClassic.TYPE_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
    }
}
