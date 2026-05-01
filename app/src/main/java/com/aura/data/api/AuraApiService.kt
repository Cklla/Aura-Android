package com.aura.data.api

import retrofit2.http.Body
import retrofit2.http.POST

// Interface = contrat. On décrit quelles routes existent et comment les appeler. Retrofit fait tout le reste (requêtes HTTP, parsing JSON, etc)
interface AuraApiService {

    // @POST : c'est une requête HTTP POST (on envoie des données)
    // "user/login" : le chemin de la route (s'ajoute à l'URL de base)
    // suspend : obligatoire pour les coroutines (la fonction peut être "mise en pause")
    // @Body : le corps de la requête (l'objet LoginRequest sera converti en JSON)
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
