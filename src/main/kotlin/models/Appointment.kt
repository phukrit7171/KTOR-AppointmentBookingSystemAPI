package com.camt.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

@Serializable
data class Appointment(
    val id: Int? = null,
    val clientName: String,
    val clientEmail: String,
    val appointmentTime: LocalDateTime,
    val serviceId: Int,
    val serviceName: String? = null,
    val durationInMinutes: Int? = null
)

@Serializable
data class AppointmentRequest(
    val clientName: String,
    val clientEmail: String,
    val appointmentTime: LocalDateTime,
    val serviceId: Int
)

@Serializable
data class AppointmentResponse(
    val success: Boolean,
    val message: String,
    val appointment: Appointment? = null
)