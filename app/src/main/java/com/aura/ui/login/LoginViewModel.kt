package com.aura.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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

    // Appelé depuis l'Activity quand l'utilisateur tape dans le champ email
    fun onEmailChanged(email: String) {
        _email.value = email
    }

    // Appelé depuis l'Activity quand l'utilisateur tape dans le champ mot de passe
    fun onPasswordChanged(password: String) {
        _password.value = password
    }
}