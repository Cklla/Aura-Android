package com.aura.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.data.api.LoginRequest
import com.aura.data.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Sealed class = une liste fermée d'états possibles. Impossible d'en ajouter depuis l'extérieur.
sealed class LoginUiState {
    object Idle : LoginUiState()       // état initial, rien ne se passe
    object Loading : LoginUiState()    // requête en cours
    object Success : LoginUiState()    // connexion réussie
    data class Error(val message: String) : LoginUiState() // erreur avec message
}

class LoginViewModel : ViewModel() {

    // Les deux champs du formulaire, initialement vides
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")

    // Le bouton est actif seulement si les deux champs sont remplis
    // combine() fusionne deux Flow : dès que l'un change, il recalcule
    val isLoginButtonEnabled: StateFlow<Boolean> = combine(_email, _password) { email, password ->
        email.isNotBlank() && password.isNotBlank()
    }.stateIn(
        scope = viewModelScope, // lié au cycle de vie du ViewModel
        started = SharingStarted.WhileSubscribed(5000), // actif tant qu'il y a un observateur
        initialValue = false // bouton désactivé au démarrage
    )

    // L'état de l'UI (Loading, Success, Error...)
    // private _uiState modifiable seulement ici, uiState en lecture seule depuis l'Activity
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChanged(email: String) { _email.value = email }
    fun onPasswordChanged(password: String) { _password.value = password }

    fun login(identifier: String, password: String) {
        // viewModelScope.launch : lance une coroutine liée au ViewModel
        // Si le ViewModel est détruit, la coroutine s'annule automatiquement
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading // on passe en état "chargement"

            try {
                // suspend fun : on "attend" ici que la réponse arrive, sans bloquer l'UI
                val response = RetrofitInstance.api.login(LoginRequest(identifier, password))

                if (response.granted) {
                    _uiState.value = LoginUiState.Success
                } else {
                    // L'API a répondu, mais la connexion est refusée
                    _uiState.value = LoginUiState.Error("Identifiants incorrects")
                }
            } catch (e: Exception) {
                // Problème réseau (pas de connexion, serveur hors ligne, etc.)
                _uiState.value = LoginUiState.Error("Erreur réseau : ${e.message}")
            }
        }
    }
}