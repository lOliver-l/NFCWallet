package com.nfcwallet.app.data.mapper

import com.nfcwallet.app.data.local.NfcCardEntity
import com.nfcwallet.app.nfc.model.NfcCardInfo

fun NfcCardInfo.toEntity(name: String): NfcCardEntity {
    return NfcCardEntity(
        name = name,
        uid = this.uid,
        cardType = this.mifareType,
        technologies = this.techList.joinToString(","),
        hasNfcA = this.hasNfcA,
        atqa = this.atqa,
        sak = this.sak,
        hasMifareClassic = this.hasMifareClassic,
        size = this.size,
        sectorCount = this.sectorCount,
        blockCount = this.blockCount
    )
}

fun NfcCardEntity.toInfo(): NfcCardInfo {
    return NfcCardInfo(
        uid = this.uid,
        techList = this.technologies.split(",").filter { it.isNotBlank() },
        hasNfcA = this.hasNfcA,
        atqa = this.atqa,
        sak = this.sak,
        hasMifareClassic = this.hasMifareClassic,
        mifareType = this.cardType,
        size = this.size,
        sectorCount = this.sectorCount,
        blockCount = this.blockCount
    )
}
