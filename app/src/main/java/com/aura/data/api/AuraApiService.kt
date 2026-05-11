package com.aura.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

// Interface = contrat. On décrit quelles routes existent et comment les appeler. Retrofit fait tout le reste (requêtes HTTP, parsing JSON, etc)
interface AuraApiService {

    // @POST : c'est une requête HTTP POST (on envoie des données)
    // "login" : le chemin de la route (s'ajoute à l'URL de base)
    // suspend : obligatoire pour les coroutines (la fonction peut être "mise en pause")
    // @Body : le corps de la requête (l'objet LoginRequest sera converti en JSON)
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // @GET : requête HTTP GET (on demande des données, on n'en envoie pas)
    // "accounts/{id}" : le {id} est un paramètre dynamique (remplacé par le vrai userID)
    // @Path("id") : Retrofit remplace {id} dans l'URL par la valeur de ce paramètre
    // List<AccountResponse> : l'API retourne une liste de comptes
    @GET("accounts/{id}")
    suspend fun getAccounts(@Path("id") userID: String): List<AccountResponse>
}
