package com.appointment.services

import com.appointment.models.*
import com.appointment.repositories.AppointmentRepository
import com.appointment.repositories.ServiceRepository
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes

class AppointmentServiceImpl(
    private val appointmentRepository: AppointmentRepository,
    private val serviceRepository: ServiceRepository
) {
    
    suspend fun getAllAppointments(): List<AppointmentResponse> {
        val appointments = appointmentRepository.getAllAppointments()
        return appointments.map { appointment ->
            val service = serviceRepository.getServiceById(appointment.serviceId)
                ?: throw ServiceNotFoundException("Service with ID ${appointment.serviceId} not found")
            appointment.toResponse(service)
        }
    }
    
    suspend fun getAppointmentById(id: Int): AppointmentResponse {
        val appointment = appointmentRepository.getAppointmentById(id)
            ?: throw AppointmentNotFoundException("Appointment with ID $id not found")
        
        val service = serviceRepository.getServiceById(appointment.serviceId)
            ?: throw ServiceNotFoundException("Service with ID ${appointment.serviceId} not found")
        
        return appointment.toResponse(service)
    }
    
    suspend fun createAppointment(request: AppointmentRequest): AppointmentResponse {
        // Validate request
        validateAppointmentRequest(request)
        
        // Get service to check duration
        val service = serviceRepository.getServiceById(request.serviceId)
            ?: throw ServiceNotFoundException("Service with ID ${request.serviceId} not found")
        
        // Check for conflicting appointments
        val endTime = request.appointmentTime
            .toInstant(TimeZone.currentSystemDefault())
            .plus(service.defaultDurationInMinutes.minutes)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val conflictingAppointments = appointmentRepository.getConflictingAppointments(
            serviceId = request.serviceId,
            startTime = request.appointmentTime,
            endTime = endTime
        )
        
        if (conflictingAppointments.isNotEmpty()) {
            throw DoubleBookingException("The requested time slot conflicts with an existing appointment")
        }
        
        // Create appointment
        val appointment = appointmentRepository.createAppointment(request)
        return appointment.toResponse(service)
    }
    
    suspend fun updateAppointment(id: Int, request: AppointmentRequest): AppointmentResponse {
        // Validate request
        validateAppointmentRequest(request)
        
        // Get service to check duration
        val service = serviceRepository.getServiceById(request.serviceId)
            ?: throw ServiceNotFoundException("Service with ID ${request.serviceId} not found")
        
        // Check for conflicting appointments (excluding the current appointment)
        val endTime = request.appointmentTime
            .toInstant(TimeZone.currentSystemDefault())
            .plus(service.defaultDurationInMinutes.minutes)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val conflictingAppointments = appointmentRepository.getConflictingAppointments(
            serviceId = request.serviceId,
            startTime = request.appointmentTime,
            endTime = endTime,
            excludeId = id
        )
        
        if (conflictingAppointments.isNotEmpty()) {
            throw DoubleBookingException("The requested time slot conflicts with an existing appointment")
        }
        
        // Update appointment
        val appointment = appointmentRepository.updateAppointment(id, request)
            ?: throw AppointmentNotFoundException("Appointment with ID $id not found")
        
        return appointment.toResponse(service)
    }
    
    suspend fun deleteAppointment(id: Int): Boolean {
        val deleted = appointmentRepository.deleteAppointment(id)
        if (!deleted) {
            throw AppointmentNotFoundException("Appointment with ID $id not found")
        }
        return true
    }
    
    private suspend fun validateAppointmentRequest(request: AppointmentRequest) {
        // Client name and email cannot be blank
        if (request.clientName.isBlank()) {
            throw IllegalArgumentException("Client name cannot be blank")
        }
        
        if (request.clientEmail.isBlank()) {
            throw IllegalArgumentException("Client email cannot be blank")
        }
        
        // Email must contain "@" symbol
        if (!request.clientEmail.contains("@")) {
            throw IllegalArgumentException("Invalid email format")
        }
        
        // Appointment time must be in the future
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (request.appointmentTime <= now) {
            throw InvalidDateTimeException("Appointment time must be in the future")
        }
        
        // Service must exist
        if (!serviceRepository.serviceExists(request.serviceId)) {
            throw ServiceNotFoundException("Service with ID ${request.serviceId} not found")
        }
    }
    
    private fun Appointment.toResponse(service: Service): AppointmentResponse {
        return AppointmentResponse(
            id = this.id ?: throw IllegalStateException("Appointment ID is null"),
            clientName = this.clientName,
            clientEmail = this.clientEmail,
            appointmentTime = this.appointmentTime,
            service = service
        )
    }
}