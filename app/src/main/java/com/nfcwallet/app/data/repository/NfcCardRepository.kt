package com.nfcwallet.app.data.repository

import com.nfcwallet.app.data.local.NfcCardDao
import com.nfcwallet.app.data.local.NfcCardEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NfcCardRepository(private val dao: NfcCardDao) {

    fun getAllSavedCards(): Flow<List<NfcCardEntity>> {
        return dao.getAllCards()
    }

    suspend fun saveCard(card: NfcCardEntity): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getCardByUid(card.uid)
        if (existing != null) {
            return@withContext false
        }
        dao.insertCard(card)
        return@withContext true
    }

    suspend fun deleteCard(card: NfcCardEntity) = withContext(Dispatchers.IO) {
        dao.deleteCard(card)
    }
}
