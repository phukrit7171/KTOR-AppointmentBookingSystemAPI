package com.camt.common

import com.camt.models.*
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random

/**
 * Utility functions for testing
 */
object TestUtils {
    
    /**
     * Creates a random service request for testing
     */
    fun createRandomServiceRequest(): ServiceRequest {
        val serviceTypes = listOf(
            "Haircut", "Massage", "Consultation", "Therapy", "Manicure", 
            "Pedicure", "Facial", "Cleaning", "Repair", "Installation"
        )
        
        val descriptions = listOf(
            "Professional service", "High-quality service", "Expert service",
            "Premium service", "Standard service", "Basic service"
        )
        
        val durations = listOf(30, 45, 60, 90, 120)
        
        return ServiceRequest(
            name = serviceTypes.random(),
            description = descriptions.random(),
            defaultDurationInMinutes = durations.random()
        )
    }
    
    /**
     * Creates a random appointment request for testing
     */
    fun createRandomAppointmentRequest(serviceId: Int): AppointmentRequest {
        val firstNames = listOf(
            "John", "Jane", "Michael", "Sarah", "David", "Emily",
            "Robert", "Lisa", "James", "Maria", "William", "Jennifer"
        )
        
        val lastNames = listOf(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez"
        )
        
        val firstName = firstNames.random()
        val lastName = lastNames.random()
        val email = "${firstName.lowercase()}.${lastName.lowercase()}@example.com"
        
        // Generate random date/time within reasonable range
        val year = 2024
        val month = Random.nextInt(1, 13)
        val day = Random.nextInt(1, 28) // Use 28 to avoid month-specific issues
        val hour = Random.nextInt(8, 18) // Business hours 8 AM to 6 PM
        val minute = listOf(0, 15, 30, 45).random() // Common appointment times
        
        val appointmentTime = LocalDateTime(year, month, day, hour, minute)
        
        return AppointmentRequest(
            clientName = "$firstName $lastName",
            clientEmail = email,
            appointmentTime = appointmentTime,
            serviceId = serviceId
        )
    }
    
    /**
     * Creates a list of non-overlapping appointment requests
     */
    fun createNonOverlappingAppointments(
        serviceId: Int,
        count: Int,
        startTime: LocalDateTime = LocalDateTime.parse("2024-12-01T09:00:00"),
        serviceDuration: Int = 60
    ): List<AppointmentRequest> {
        val appointments = mutableListOf<AppointmentRequest>()
        var currentTime = startTime
        
        repeat(count) { index ->
            val appointment = AppointmentRequest(
                clientName = "Client ${index + 1}",
                clientEmail = "client${index + 1}@example.com",
                appointmentTime = currentTime,
                serviceId = serviceId
            )
            
            appointments.add(appointment)
            // Add service duration plus 30 minutes buffer
            currentTime = currentTime.plusMinutes(serviceDuration + 30)
        }
        
        return appointments
    }
    
    /**
     * Creates overlapping appointment requests for testing conflicts
     */
    fun createOverlappingAppointments(
        serviceId: Int,
        baseTime: LocalDateTime = LocalDateTime.parse("2024-12-01T10:00:00"),
        serviceDuration: Int = 60
    ): List<AppointmentRequest> {
        return listOf(
            AppointmentRequest(
                clientName = "Base Client",
                clientEmail = "base@example.com",
                appointmentTime = baseTime,
                serviceId = serviceId
            ),
            AppointmentRequest(
                clientName = "Overlapping Client 1",
                clientEmail = "overlap1@example.com",
                appointmentTime = baseTime.plusMinutes(30), // Overlaps with base
                serviceId = serviceId
            ),
            AppointmentRequest(
                clientName = "Overlapping Client 2",
                clientEmail = "overlap2@example.com",
                appointmentTime = baseTime.plusMinutes(-30), // Overlaps with base
                serviceId = serviceId
            ),
            AppointmentRequest(
                clientName = "Exact Same Time",
                clientEmail = "same@example.com",
                appointmentTime = baseTime, // Exact same time as base
                serviceId = serviceId
            )
        )
    }
    
    /**
     * Creates a service request with specific parameters
     */
    fun createServiceRequest(
        name: String = "Test Service",
        description: String = "A test service",
        duration: Int = 60
    ): ServiceRequest {
        return ServiceRequest(
            name = name,
            description = description,
            defaultDurationInMinutes = duration
        )
    }
    
    /**
     * Creates an appointment request with specific parameters
     */
    fun createAppointmentRequest(
        clientName: String = "Test Client",
        clientEmail: String = "test@example.com",
        appointmentTime: LocalDateTime = LocalDateTime.parse("2024-12-01T10:00:00"),
        serviceId: Int
    ): AppointmentRequest {
        return AppointmentRequest(
            clientName = clientName,
            clientEmail = clientEmail,
            appointmentTime = appointmentTime,
            serviceId = serviceId
        )
    }
    
    /**
     * Extension function to add minutes to LocalDateTime
     */
    private fun LocalDateTime.plusMinutes(minutes: Int): LocalDateTime {
        val totalMinutes = this.minute + minutes
        val additionalHours = totalMinutes / 60
        val newMinutes = totalMinutes % 60
        
        return LocalDateTime(
            this.year,
            this.monthNumber,
            this.dayOfMonth,
            this.hour + additionalHours,
            if (newMinutes < 0) newMinutes + 60 else newMinutes
        )
    }
    
    /**
     * Validates that an ApiResponse has the expected structure
     */
    fun <T> validateApiResponse(
        response: ApiResponse<T>,
        expectedSuccess: Boolean,
        expectedMessage: String? = null,
        expectedDataNotNull: Boolean = false
    ): Boolean {
        if (response.success != expectedSuccess) return false
        if (expectedMessage != null && response.message != expectedMessage) return false
        if (expectedDataNotNull && response.data == null) return false
        return true
    }
    
    /**
     * Validates that a Service has the expected values
     */
    fun validateService(
        service: Service,
        expectedName: String? = null,
        expectedDescription: String? = null,
        expectedDuration: Int? = null,
        expectedIdNotNull: Boolean = false
    ): Boolean {
        if (expectedName != null && service.name != expectedName) return false
        if (expectedDescription != null && service.description != expectedDescription) return false
        if (expectedDuration != null && service.defaultDurationInMinutes != expectedDuration) return false
        if (expectedIdNotNull && service.id == null) return false
        return true
    }
    
    /**
     * Validates that an Appointment has the expected values
     */
    fun validateAppointment(
        appointment: Appointment,
        expectedClientName: String? = null,
        expectedClientEmail: String? = null,
        expectedServiceId: Int? = null,
        expectedServiceName: String? = null,
        expectedIdNotNull: Boolean = false
    ): Boolean {
        if (expectedClientName != null && appointment.clientName != expectedClientName) return false
        if (expectedClientEmail != null && appointment.clientEmail != expectedClientEmail) return false
        if (expectedServiceId != null && appointment.serviceId != expectedServiceId) return false
        if (expectedServiceName != null && appointment.serviceName != expectedServiceName) return false
        if (expectedIdNotNull && appointment.id == null) return false
        return true
    }
    
    /**
     * Creates test data for bulk operations
     */
    fun createBulkTestData(
        numberOfServices: Int = 10,
        appointmentsPerService: Int = 5
    ): Pair<List<ServiceRequest>, Map<Int, List<AppointmentRequest>>> {
        val services = (1..numberOfServices).map { i ->
            ServiceRequest(
                name = "Bulk Service $i",
                description = "Service for bulk testing $i",
                defaultDurationInMinutes = 60
            )
        }
        
        // This will be populated after services are created with their IDs
        val appointmentsByService = mapOf<Int, List<AppointmentRequest>>()
        
        return Pair(services, appointmentsByService)
    }
    
    /**
     * Common test scenarios for edge cases
     */
    object EdgeCases {
        val longString = "A".repeat(1000)
        val specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        val unicodeString = "测试 Test тест テスト"
        val emptyString = ""
        val whitespaceString = "   "
        
        fun createEdgeCaseServiceRequest(): ServiceRequest {
            return ServiceRequest(
                name = unicodeString,
                description = longString,
                defaultDurationInMinutes = 1440 // 24 hours
            )
        }
        
        fun createEdgeCaseAppointmentRequest(serviceId: Int): AppointmentRequest {
            return AppointmentRequest(
                clientName = unicodeString,
                clientEmail = "test@${specialCharacters.filter { it.isLetterOrDigit() }}.com",
                appointmentTime = LocalDateTime.parse("2024-12-31T23:59:59"),
                serviceId = serviceId
            )
        }
    }
    
    /**
     * Common HTTP status codes used in tests
     */
    object HttpStatusCodes {
        const val OK = 200
        const val CREATED = 201
        const val BAD_REQUEST = 400
        const val NOT_FOUND = 404
        const val CONFLICT = 409
        const val INTERNAL_SERVER_ERROR = 500
    }
    
    /**
     * Common test messages
     */
    object TestMessages {
        const val SERVICE_CREATED = "Service created successfully"
        const val SERVICE_UPDATED = "Service updated successfully"
        const val SERVICE_DELETED = "Service deleted successfully"
        const val SERVICE_RETRIEVED = "Service retrieved successfully"
        const val SERVICES_RETRIEVED = "Services retrieved successfully"
        const val SERVICE_NOT_FOUND = "Service not found"
        const val INVALID_SERVICE_ID = "Invalid service ID"
        const val CANNOT_DELETE_SERVICE = "Cannot delete service with existing appointments"
        
        const val APPOINTMENT_CREATED = "Appointment created successfully"
        const val APPOINTMENT_UPDATED = "Appointment updated successfully"
        const val APPOINTMENT_DELETED = "Appointment deleted successfully"
        const val APPOINTMENT_RETRIEVED = "Appointment retrieved successfully"
        const val APPOINTMENTS_RETRIEVED = "Appointments retrieved successfully"
        const val APPOINTMENT_NOT_FOUND = "Appointment not found"
        const val INVALID_APPOINTMENT_ID = "Invalid appointment ID"
        const val CANNOT_CREATE_APPOINTMENT = "Cannot create appointment: Service not found or time slot already booked"
        const val CANNOT_UPDATE_APPOINTMENT = "Cannot update appointment: Service not found, appointment not found, or time slot already booked"
    }
} 