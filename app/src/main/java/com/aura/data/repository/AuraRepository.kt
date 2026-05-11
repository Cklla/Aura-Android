package com.aura.data.repository

import com.aura.data.api.AuraApiService
import com.aura.data.api.LoginRequest

class AuraRepository(private val api: AuraApiService) {

    // Le Repository expose des méthodes métier simples. Le ViewModel ne voit pas LoginRequest, il donne juste deux Strings
    suspend fun login(identifier: String, password: String): Boolean {
        val response = api.login(LoginRequest(identifier, password))
        return response.granted // true = accès autorisé, false = refusé
    }
}