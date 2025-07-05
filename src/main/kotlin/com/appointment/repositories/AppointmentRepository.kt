// src/main/kotlin/com/appointment/repositories/AppointmentRepository.kt
package com.appointment.repositories

import com.appointment.database.DatabaseFactory.dbQuery
import com.appointment.models.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.dao.id.EntityID
import kotlin.time.Duration.Companion.minutes

interface AppointmentRepository {
    suspend fun getAllAppointments(): List<Appointment>
    suspend fun getAppointmentById(id: Int): Appointment?
    suspend fun createAppointment(appointment: AppointmentRequest): Appointment
    suspend fun updateAppointment(id: Int, appointment: AppointmentRequest): Appointment?
    suspend fun deleteAppointment(id: Int): Boolean
    suspend fun getConflictingAppointments(
        serviceId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        excludeId: Int? = null
    ): List<Appointment>
    suspend fun hasConflictingAppointment(
        appointmentTime: LocalDateTime,
        durationMinutes: Int,
        excludeId: Int? = null
    ): Boolean
}

class AppointmentRepositoryImpl : AppointmentRepository {
    override suspend fun getAllAppointments(): List<Appointment> = dbQuery {
        AppointmentEntity.all().map { it.toModel() }
    }

    override suspend fun getAppointmentById(id: Int): Appointment? = dbQuery {
        AppointmentEntity.findById(EntityID(id, AppointmentsTable))?.toModel()
    }

    override suspend fun createAppointment(appointment: AppointmentRequest): Appointment = dbQuery {
        AppointmentEntity.new {
            clientName = appointment.clientName
            clientEmail = appointment.clientEmail
            appointmentTime = appointment.appointmentTime
            serviceId = EntityID(appointment.serviceId, ServicesTable)
        }.toModel()
    }

    override suspend fun updateAppointment(id: Int, appointment: AppointmentRequest): Appointment? = dbQuery {
        AppointmentEntity.findById(EntityID(id, AppointmentsTable))?.apply {
            clientName = appointment.clientName
            clientEmail = appointment.clientEmail
            appointmentTime = appointment.appointmentTime
            serviceId = EntityID(appointment.serviceId, ServicesTable)
        }?.toModel()
    }

    override suspend fun deleteAppointment(id: Int): Boolean = dbQuery {
        AppointmentEntity.findById(EntityID(id, AppointmentsTable))?.let {
            it.delete()
            true
        } ?: false
    }

    override suspend fun getConflictingAppointments(
        serviceId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        excludeId: Int?
    ): List<Appointment> = dbQuery {
        val query = AppointmentEntity.find {
            (AppointmentsTable.serviceId eq EntityID(serviceId, ServicesTable)) and
                    (AppointmentsTable.appointmentTime greaterEq startTime) and
                    (AppointmentsTable.appointmentTime less endTime)
        }

        val filtered = if (excludeId != null) {
            query.filter { it.id.value != excludeId }
        } else {
            query.toList()
        }

        filtered.map { it.toModel() }
    }

    override suspend fun hasConflictingAppointment(
        appointmentTime: LocalDateTime,
        durationMinutes: Int,
        excludeId: Int?
    ): Boolean = dbQuery {
        val endTime = appointmentTime
            .toInstant(TimeZone.currentSystemDefault())
            .plus(durationMinutes.minutes)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val conflictingAppointments = getConflictingAppointments(
            serviceId = 0, // This will be filtered by the calling service
            startTime = appointmentTime,
            endTime = endTime,
            excludeId = excludeId
        )
        conflictingAppointments.isNotEmpty()
    }
}