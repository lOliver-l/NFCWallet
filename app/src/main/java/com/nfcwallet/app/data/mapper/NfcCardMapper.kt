package com.nfcwallet.app.data.mapper

import com.nfcwallet.app.data.local.NfcCardEntity
import com.nfcwallet.app.nfc.model.NfcCardInfo

fun NfcCardInfo.toEntity(
    name: String,
    category: String = "Other",
    iconName: String = "card",
    colorHex: String = "blue",
    isFavorite: Boolean = false
): NfcCardEntity {
    return NfcCardEntity(
        name = name,
        uid = this.uid,
        cardType = this.mifareType,
        technologies = this.techList.joinToString(","),
        hasNfcA = this.hasNfcA,
        hasNfcB = this.hasNfcB,
        hasNfcF = this.hasNfcF,
        hasNfcV = this.hasNfcV,
        isIsoDep = this.isIsoDep,
        isNdef = this.isNdef,
        ndefContent = this.ndefContent ?: "",
        atqa = this.atqa,
        sak = this.sak,
        hasMifareClassic = this.hasMifareClassic,
        hasMifareUltralight = this.hasMifareUltralight,
        size = this.size,
        sectorCount = this.sectorCount,
        blockCount = this.blockCount,
        category = category,
        iconName = iconName,
        colorHex = colorHex,
        isFavorite = isFavorite
    )
}

fun NfcCardEntity.toInfo(): NfcCardInfo {
    return NfcCardInfo(
        uid = this.uid,
        techList = this.technologies.split(",").filter { it.isNotBlank() },
        hasNfcA = this.hasNfcA,
        hasNfcB = this.hasNfcB,
        hasNfcF = this.hasNfcF,
        hasNfcV = this.hasNfcV,
        isIsoDep = this.isIsoDep,
        isNdef = this.isNdef,
        ndefContent = this.ndefContent.ifEmpty { null },
        atqa = this.atqa,
        sak = this.sak,
        hasMifareClassic = this.hasMifareClassic,
        hasMifareUltralight = this.hasMifareUltralight,
        mifareType = this.cardType,
        size = this.size,
        sectorCount = this.sectorCount,
        blockCount = this.blockCount
    )
}
