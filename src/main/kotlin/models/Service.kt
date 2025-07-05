package com.camt.models

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: Int? = null,
    val name: String,
    val description: String,
    val defaultDurationInMinutes: Int
)

@Serializable
data class ServiceRequest(
    val name: String,
    val description: String,
    val defaultDurationInMinutes: Int
)