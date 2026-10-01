package com.nfcwallet.app.nfc.reader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import com.nfcwallet.app.nfc.model.NfcCardInfo
import com.nfcwallet.app.nfc.utils.NfcUtils

// Handles the logic of reading and parsing NFC tags
class NfcReader(private val onCardDetected: (NfcCardInfo) -> Unit) : NfcAdapter.ReaderCallback {

    // Called on a background thread when a tag is discovered
    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return

        val uid = NfcUtils.bytesToHex(tag.id)
        val techList = tag.techList.map { it.substringAfterLast('.') }
        
        val nfcA = NfcA.get(tag)
        val mifare = MifareClassic.get(tag)

        val cardInfo = NfcCardInfo(
            uid = uid,
            techList = techList,
            hasNfcA = nfcA != null,
            atqa = nfcA?.atqa?.let { NfcUtils.bytesToHex(it) } ?: "N/A",
            sak = nfcA?.sak?.let { "%02X".format(it) } ?: "N/A",
            hasMifareClassic = mifare != null,
            mifareType = mifare?.type?.let { NfcUtils.getMifareTypeString(it) } ?: "N/A",
            size = mifare?.size ?: 0,
            sectorCount = mifare?.sectorCount ?: 0,
            blockCount = mifare?.blockCount ?: 0
        )

        onCardDetected(cardInfo)
    }
}
