package com.aura.data.repository

import com.aura.data.api.AuraApiService
import com.aura.data.api.LoginRequest
import com.aura.data.api.AccountResponse

class AuraRepository(private val api: AuraApiService) {

    // Le Repository expose des méthodes métier simples. Le ViewModel ne voit pas LoginRequest, il donne juste deux Strings
    suspend fun login(identifier: String, password: String): Boolean {
        val response = api.login(LoginRequest(identifier, password))
        return response.granted // true = accès autorisé, false = refusé
    }

    // Récupère la liste des comptes puis filtre pour ne garder que le compte principal
    // first() plante si aucun compte n'est "main = true"
    suspend fun getAccounts(userId: String): Double {
        val accounts = api.getAccounts(userId)
        val mainAccount = accounts.first { it.main }
        return mainAccount.balance
    }
}