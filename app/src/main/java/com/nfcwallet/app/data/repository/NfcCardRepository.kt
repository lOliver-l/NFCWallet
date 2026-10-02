package com.nfcwallet.app.data.repository

import com.nfcwallet.app.data.local.NfcCardDao
import com.nfcwallet.app.data.local.NfcCardEntity
import com.nfcwallet.app.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NfcCardRepository(
    private val dao: NfcCardDao,
    private val cryptoManager: CryptoManager = CryptoManager()
) {

    // Returns flow of saved cards with sensitive fields decrypted for UI display
    fun getAllSavedCards(): Flow<List<NfcCardEntity>> {
        return dao.getAllCards().map { list ->
            list.map { card -> decryptEntity(card) }
        }
    }

    // Encrypts sensitive fields before storing and checks for duplicate UIDs
    suspend fun saveCard(card: NfcCardEntity): Boolean = withContext(Dispatchers.IO) {
        val allCards = dao.getAllCardsSync()
        val duplicate = allCards.any { existing ->
            cryptoManager.decrypt(existing.uid) == card.uid && existing.id != card.id
        }
        if (duplicate) {
            return@withContext false
        }

        val encryptedEntity = encryptEntity(card)
        if (card.id > 0) {
            dao.updateCard(encryptedEntity)
        } else {
            dao.insertCard(encryptedEntity)
        }
        return@withContext true
    }

    suspend fun updateCard(card: NfcCardEntity) = withContext(Dispatchers.IO) {
        val encryptedEntity = encryptEntity(card)
        dao.updateCard(encryptedEntity)
    }

    suspend fun deleteCard(card: NfcCardEntity) = withContext(Dispatchers.IO) {
        val encryptedEntity = encryptEntity(card)
        dao.deleteCard(encryptedEntity)
    }

    private fun encryptEntity(card: NfcCardEntity): NfcCardEntity {
        return card.copy(
            uid = cryptoManager.encrypt(card.uid),
            technologies = cryptoManager.encrypt(card.technologies),
            atqa = cryptoManager.encrypt(card.atqa),
            sak = cryptoManager.encrypt(card.sak)
        )
    }

    private fun decryptEntity(card: NfcCardEntity): NfcCardEntity {
        return card.copy(
            uid = cryptoManager.decrypt(card.uid),
            technologies = cryptoManager.decrypt(card.technologies),
            atqa = cryptoManager.decrypt(card.atqa),
            sak = cryptoManager.decrypt(card.sak)
        )
    }
}
