package com.aura.data.api

// Un compte bancaire tel que l'API nous le retourne.
// main = true indique le compte principal (celui dont on veut la balance)
data class AccountResponse(
    val id: String,
    val main: Boolean,
    val balance: Double
)
