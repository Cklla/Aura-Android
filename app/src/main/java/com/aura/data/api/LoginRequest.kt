package com.aura.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class LoginRequest(
    // @Json(name = "login") : le nom du champ dans le JSON envoyé à l'API
    // l'API attend "id", pas "identifier"
    @Json(name = "id") val identifier: String,
    @Json(name = "password") val password: String
)
