package com.camt.database

import com.camt.models.Service
import com.camt.models.ServiceRequest
import com.camt.models.Appointment
import com.camt.models.AppointmentRequest
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import org.jetbrains.exposed.sql.selectAll

class DatabaseManager {

    // Service operations
    fun getAllServices(): List<Service> = transaction {
        Services.selectAll().map { row ->
            Service(
                id = row[Services.id].value,
                name = row[Services.name],
                description = row[Services.description],
                defaultDurationInMinutes = row[Services.defaultDurationInMinutes]
            )
        }
    }

    fun getServiceById(id: Int): Service? = transaction {
        Services.selectAll().where { Services.id eq id }
            .map { row ->
                Service(
                    id = row[Services.id].value,
                    name = row[Services.name],
                    description = row[Services.description],
                    defaultDurationInMinutes = row[Services.defaultDurationInMinutes]
                )
            }.singleOrNull()
    }

    fun createService(request: ServiceRequest): Service = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val insertResult = Services.insert {
            it[name] = request.name
            it[description] = request.description
            it[defaultDurationInMinutes] = request.defaultDurationInMinutes
            it[createdAt] = now
            it[updatedAt] = now
        }

        Service(
            id = insertResult[Services.id].value,
            name = request.name,
            description = request.description,
            defaultDurationInMinutes = request.defaultDurationInMinutes
        )
    }

    fun updateService(id: Int, request: ServiceRequest): Service? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val updateCount = Services.update({ Services.id eq id }) {
            it[name] = request.name
            it[description] = request.description
            it[defaultDurationInMinutes] = request.defaultDurationInMinutes
            it[updatedAt] = now
        }

        if (updateCount > 0) {
            getServiceById(id)
        } else null
    }

    fun deleteService(id: Int): Boolean = transaction {
        // Check if service has appointments
        val hasAppointments = Appointments.selectAll().where { Appointments.serviceId eq id }.count() > 0
        if (hasAppointments) {
            false
        } else {
            Services.deleteWhere { Services.id eq id } > 0
        }
    }

    // Appointment operations
    fun getAllAppointments(): List<Appointment> = transaction {
        (Appointments innerJoin Services)
            .selectAll()
            .orderBy(Appointments.appointmentTime)
            .map { row ->
                Appointment(
                    id = row[Appointments.id].value,
                    clientName = row[Appointments.clientName],
                    clientEmail = row[Appointments.clientEmail],
                    appointmentTime = row[Appointments.appointmentTime],
                    serviceId = row[Appointments.serviceId].value,
                    serviceName = row[Services.name],
                    durationInMinutes = row[Services.defaultDurationInMinutes]
                )
            }
    }

    fun getAppointmentById(id: Int): Appointment? = transaction {
        (Appointments innerJoin Services)
            .selectAll().where { Appointments.id eq id }
            .map { row ->
                Appointment(
                    id = row[Appointments.id].value,
                    clientName = row[Appointments.clientName],
                    clientEmail = row[Appointments.clientEmail],
                    appointmentTime = row[Appointments.appointmentTime],
                    serviceId = row[Appointments.serviceId].value,
                    serviceName = row[Services.name],
                    durationInMinutes = row[Services.defaultDurationInMinutes]
                )
            }.singleOrNull()
    }

    fun createAppointment(request: AppointmentRequest): Appointment? = transaction {
        // Check if service exists
        val service = getServiceById(request.serviceId) ?: return@transaction null

        // Check for double booking
        if (isDoubleBooking(request.appointmentTime, service.defaultDurationInMinutes)) {
            return@transaction null
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val insertResult = Appointments.insert {
            it[clientName] = request.clientName
            it[clientEmail] = request.clientEmail
            it[appointmentTime] = request.appointmentTime
            it[serviceId] = request.serviceId
            it[createdAt] = now
            it[updatedAt] = now
        }

        Appointment(
            id = insertResult[Appointments.id].value,
            clientName = request.clientName,
            clientEmail = request.clientEmail,
            appointmentTime = request.appointmentTime,
            serviceId = request.serviceId,
            serviceName = service.name,
            durationInMinutes = service.defaultDurationInMinutes
        )
    }

    fun updateAppointment(id: Int, request: AppointmentRequest): Appointment? = transaction {
        // Check if service exists
        val service = getServiceById(request.serviceId) ?: return@transaction null

        // Check for double booking (exclude current appointment)
        if (isDoubleBooking(request.appointmentTime, service.defaultDurationInMinutes, excludeId = id)) {
            return@transaction null
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val updateCount = Appointments.update({ Appointments.id eq id }) {
            it[clientName] = request.clientName
            it[clientEmail] = request.clientEmail
            it[appointmentTime] = request.appointmentTime
            it[serviceId] = request.serviceId
            it[updatedAt] = now
        }

        if (updateCount > 0) {
            getAppointmentById(id)
        } else null
    }

    fun deleteAppointment(id: Int): Boolean = transaction {
        Appointments.deleteWhere { Appointments.id eq id } > 0
    }

    private fun isDoubleBooking(
        appointmentTime: LocalDateTime,
        durationInMinutes: Int,
        excludeId: Int? = null
    ): Boolean = transaction {
        val appointmentStart = appointmentTime
        val appointmentEnd = appointmentTime.plusMinutes(durationInMinutes)

        // Find overlapping appointments
        val existingAppointments = if (excludeId != null) {
            (Appointments innerJoin Services)
                .selectAll().where { Appointments.id neq excludeId }
        } else {
            (Appointments innerJoin Services)
                .selectAll()
        }

        existingAppointments.any { row ->
            val existingStart = row[Appointments.appointmentTime]
            val existingEnd = existingStart.plusMinutes(row[Services.defaultDurationInMinutes])

            // Check if appointments overlap
            appointmentStart < existingEnd && appointmentEnd > existingStart
        }
    }

    private fun LocalDateTime.plusMinutes(minutes: Int): LocalDateTime {
        // Using kotlinx-datetime's proper date arithmetic
        val instant = this.toInstant(kotlinx.datetime.TimeZone.UTC)
        val duration = kotlin.time.Duration.parse("${minutes}m")
        return (instant + duration).toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
    }
}
