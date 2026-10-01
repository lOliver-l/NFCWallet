package com.nfcwallet.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nfcwallet.app.data.local.NfcCardEntity
import com.nfcwallet.app.data.mapper.toEntity
import com.nfcwallet.app.data.repository.NfcCardRepository
import com.nfcwallet.app.nfc.model.NfcCardInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NfcCardViewModel(private val repository: NfcCardRepository) : ViewModel() {

    val savedCards = repository.getAllSavedCards().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun saveCard(cardInfo: NfcCardInfo, name: String) {
        viewModelScope.launch {
            val entity = cardInfo.toEntity(name)
            val success = repository.saveCard(entity)
            if (!success) {
                _events.emit("This card is already saved")
            } else {
                _events.emit("Card saved successfully")
            }
        }
    }

    fun deleteCard(cardEntity: NfcCardEntity) {
        viewModelScope.launch {
            repository.deleteCard(cardEntity)
        }
    }
}

class NfcCardViewModelFactory(private val repository: NfcCardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NfcCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NfcCardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
