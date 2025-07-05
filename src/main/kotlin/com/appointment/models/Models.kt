// src/main/kotlin/com/appointment/models/Models.kt
package com.appointment.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

// Database Tables
object ServicesTable : IntIdTable("services") {
    val name = varchar("name", 100)
    val description = text("description")
    val defaultDurationInMinutes = integer("default_duration_minutes")
}

object AppointmentsTable : IntIdTable("appointments") {
    val clientName = varchar("client_name", 100)
    val clientEmail = varchar("client_email", 100)
    val appointmentTime = datetime("appointment_time")
    val serviceId = reference("service_id", ServicesTable)
}

// DAO Classes
class ServiceEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ServiceEntity>(ServicesTable)

    var name by ServicesTable.name
    var description by ServicesTable.description
    var defaultDurationInMinutes by ServicesTable.defaultDurationInMinutes

    fun toModel() = Service(
        id = id.value,
        name = name,
        description = description,
        defaultDurationInMinutes = defaultDurationInMinutes
    )
}

class AppointmentEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AppointmentEntity>(AppointmentsTable)

    var clientName by AppointmentsTable.clientName
    var clientEmail by AppointmentsTable.clientEmail
    var appointmentTime by AppointmentsTable.appointmentTime
    var serviceId by AppointmentsTable.serviceId

    fun toModel() = Appointment(
        id = id.value,
        clientName = clientName,
        clientEmail = clientEmail,
        appointmentTime = appointmentTime,
        serviceId = serviceId.value
    )
}

// Data Transfer Objects
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

@Serializable
data class Appointment(
    val id: Int? = null,
    val clientName: String,
    val clientEmail: String,
    val appointmentTime: LocalDateTime,
    val serviceId: Int
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
    val id: Int,
    val clientName: String,
    val clientEmail: String,
    val appointmentTime: LocalDateTime,
    val service: Service
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

// Business Logic Exceptions
class ServiceNotFoundException(message: String) : Exception(message)
class AppointmentNotFoundException(message: String) : Exception(message)
class DoubleBookingException(message: String) : Exception(message)
class InvalidDateTimeException(message: String) : Exception(message)

// Health Check Response
@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: Long
)