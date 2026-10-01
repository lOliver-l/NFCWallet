package com.nfcwallet.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NfcCardDao {
    @Query("SELECT * FROM nfc_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<NfcCardEntity>>

    @Query("SELECT * FROM nfc_cards WHERE uid = :uid LIMIT 1")
    fun getCardByUid(uid: String): NfcCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCard(card: NfcCardEntity)

    @Delete
    fun deleteCard(card: NfcCardEntity)
}
