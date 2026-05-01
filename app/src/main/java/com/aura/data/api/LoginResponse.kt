package com.aura.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Ce que l'API nous renvoie : un boolean "granted" qui indique si la connexion est ok
data class LoginResponse(
    @Json(name = "granted") val granted: Boolean
)
