package com.nfcwallet.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfc_cards")
data class NfcCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val uid: String,
    val cardType: String,
    val technologies: String,
    val hasNfcA: Boolean,
    val atqa: String,
    val sak: String,
    val hasMifareClassic: Boolean,
    val size: Int,
    val sectorCount: Int,
    val blockCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)
