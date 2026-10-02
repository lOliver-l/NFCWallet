package com.nfcwallet.app.nfc.reader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import com.nfcwallet.app.nfc.model.NfcCardInfo
import com.nfcwallet.app.nfc.utils.NfcUtils
import com.nfcwallet.app.nfc.utils.SoundEffectsManager

// Advanced Reader supporting a wide catalog of NFC standards
class NfcReader(private val onCardDetected: (NfcCardInfo) -> Unit) : NfcAdapter.ReaderCallback {

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return

        // Play native card detection beep sound effect
        SoundEffectsManager.playCardReadBeep()

        val uid = NfcUtils.bytesToHex(tag.id)
        val techList = tag.techList.map { it.substringAfterLast('.') }

        // Core Technologies
        val nfcA = NfcA.get(tag)
        val nfcB = NfcB.get(tag)
        val nfcF = NfcF.get(tag)
        val nfcV = NfcV.get(tag)

        // Advanced Protocols
        val isoDep = IsoDep.get(tag)
        val ndef = Ndef.get(tag)
        val mifareClassic = MifareClassic.get(tag)
        val mifareUltra = MifareUltralight.get(tag)

        // Extract NDEF message if available and cached
        var ndefText: String? = null
        try {
            if (ndef != null) {
                val ndefMessage = ndef.cachedNdefMessage
                if (ndefMessage != null && ndefMessage.records.isNotEmpty()) {
                    val payload = ndefMessage.records[0].payload
                    // Very basic text extraction (skips language code byte)
                    if (payload.size > 1) {
                        val languageCodeLength = (payload[0].toInt() and 0x3F)
                        val textLength = payload.size - 1 - languageCodeLength
                        if (textLength > 0) {
                            ndefText = String(payload, 1 + languageCodeLength, textLength, Charsets.UTF_8)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore NDEF parsing errors
        }

        val cardInfo = NfcCardInfo(
            uid = uid,
            techList = techList,
            hasNfcA = nfcA != null,
            hasNfcB = nfcB != null,
            hasNfcF = nfcF != null,
            hasNfcV = nfcV != null,
            isIsoDep = isoDep != null,
            isNdef = ndef != null,
            ndefContent = ndefText,
            atqa = nfcA?.atqa?.let { NfcUtils.bytesToHex(it) } ?: "N/A",
            sak = nfcA?.sak?.let { "%02X".format(it) } ?: "N/A",
            hasMifareClassic = mifareClassic != null,
            hasMifareUltralight = mifareUltra != null,
            mifareType = mifareClassic?.type?.let { NfcUtils.getMifareTypeString(it) } ?: if (mifareUltra != null) "Ultralight" else "N/A",
            size = mifareClassic?.size ?: ndef?.maxSize ?: 0,
            sectorCount = mifareClassic?.sectorCount ?: 0,
            blockCount = mifareClassic?.blockCount ?: 0
        )

        onCardDetected(cardInfo)
    }
}
