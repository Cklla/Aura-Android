package com.aura.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// object = singleton en Kotlin (une seule instance dans toute l'app)
object RetrofitInstance {

    // URL de base de l'API (10.0.2.2 = localhost depuis l'émulateur Android)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Moshi est le parseur JSON. KotlinJsonAdapterFactory lui permet de comprendre les data class Kotlin
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // lazy = créé seulement la première fois qu'on y accède (optimisation)
    val api: AuraApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuraApiService::class.java)
    }
}