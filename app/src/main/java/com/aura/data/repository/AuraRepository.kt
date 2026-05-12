package com.aura.data.repository

import com.aura.data.api.AuraApiService
import com.aura.data.api.LoginRequest
import com.aura.data.api.AccountResponse
import com.aura.data.api.TransferRequest

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

    // Effectue le virement et retourne true si ok
    suspend fun transfer(sender: String, recipient: String, amount: Double): Boolean {
        val response = api.transfer(TransferRequest(sender, recipient, amount))
        return response.result
    }
}