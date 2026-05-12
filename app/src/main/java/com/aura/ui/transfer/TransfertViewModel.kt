package com.aura.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aura.data.api.RetrofitInstance
import com.aura.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TransferUiState {
    object Idle: TransferUiState()
    object Loading: TransferUiState()
    object Success: TransferUiState() // pas de donnée, on ferme l'écran
    data class Error(val message: String) : TransferUiState()
}

// Le ViewModel reçoit le Repository en paramètre (même principe que LoginViewModel)
class TransferViewModel(private val repository: AuraRepository) : ViewModel() {

    // Panneau d'affichage pour le champ "bénéficiaire"
    private val _recipient = MutableStateFlow("")

    // Panneau d'affichage pour le champ "montant"
    private val _amount = MutableStateFlow("")

    // L'opération de transfert en cours
    private val _uiState = MutableStateFlow<TransferUiState>(TransferUiState.Idle)
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    // Appelé au clic du bouton, sender = l'ID de l'utilisateur connecté
    fun transfer(sender: String, recipient: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            try {
                val success = repository.transfer(sender, recipient, amount)
                if (success) {
                    _uiState.value = TransferUiState.Success
                } else {
                    _uiState.value = TransferUiState.Error("Transfert refusé par le serveur")
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Erreur réseau")
            }
        }
    }

    // combine() surveille les deux panneaux en même temps
    // Dès que l'un change, il recalcule si le bouton doit être actif
    val isTransferButtonEnabled: StateFlow<Boolean> = combine(_recipient, _amount) { recipient, amount ->
        // Le bouton est actif si : bénéficiaire non vide ET montant est un nombre > 0
        val amountValue = amount.toDoubleOrNull()
        recipient.isNotBlank() && amountValue != null && amountValue > 0
    }.stateIn(
        scope = viewModelScope, // Lié au cycle de vie du ViewModel
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false // Bouton désactivé au démarrage
    )

    // Appelé par l'Activity quand l'utilisateur tape dans le champ bénéficiaire
    fun onRecipientChanged(recipient: String) { _recipient.value = recipient }

    // Appelé par l'Activity quand l'utilisateur tape dans le champ montant
    fun onAmountChanged(amount: String) { _amount.value = amount }
}

// La Factory construit le ViewModel avec ses dépendances
class TransferViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransferViewModel(AuraRepository(RetrofitInstance.api)) as T
    }
}