package com.camt.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Services : IntIdTable() {
    val name = varchar("name", 100)
    val description = text("description")
    val defaultDurationInMinutes = integer("default_duration_in_minutes")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Appointments : IntIdTable() {
    val clientName = varchar("client_name", 100)
    val clientEmail = varchar("client_email", 100)
    val appointmentTime = datetime("appointment_time")
    val serviceId = reference("service_id", Services)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}