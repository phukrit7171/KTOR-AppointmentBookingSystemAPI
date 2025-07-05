package com.camt.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.*

class ModelTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    @Test
    fun testServiceSerialization() {
        val service = Service(
            id = 1,
            name = "Test Service",
            description = "A test service",
            defaultDurationInMinutes = 60
        )
        
        val serialized = json.encodeToString(Service.serializer(), service)
        val deserialized = json.decodeFromString(Service.serializer(), serialized)
        
        assertEquals(service.id, deserialized.id)
        assertEquals(service.name, deserialized.name)
        assertEquals(service.description, deserialized.description)
        assertEquals(service.defaultDurationInMinutes, deserialized.defaultDurationInMinutes)
    }
    
    @Test
    fun testServiceRequestSerialization() {
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "A test service",
            defaultDurationInMinutes = 60
        )
        
        val serialized = json.encodeToString(ServiceRequest.serializer(), serviceRequest)
        val deserialized = json.decodeFromString(ServiceRequest.serializer(), serialized)
        
        assertEquals(serviceRequest.name, deserialized.name)
        assertEquals(serviceRequest.description, deserialized.description)
        assertEquals(serviceRequest.defaultDurationInMinutes, deserialized.defaultDurationInMinutes)
    }
    
    @Test
    fun testAppointmentSerialization() {
        val appointment = Appointment(
            id = 1,
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 1,
            serviceName = "Test Service",
            durationInMinutes = 60
        )
        
        val serialized = json.encodeToString(Appointment.serializer(), appointment)
        val deserialized = json.decodeFromString(Appointment.serializer(), serialized)
        
        assertEquals(appointment.id, deserialized.id)
        assertEquals(appointment.clientName, deserialized.clientName)
        assertEquals(appointment.clientEmail, deserialized.clientEmail)
        assertEquals(appointment.appointmentTime, deserialized.appointmentTime)
        assertEquals(appointment.serviceId, deserialized.serviceId)
        assertEquals(appointment.serviceName, deserialized.serviceName)
        assertEquals(appointment.durationInMinutes, deserialized.durationInMinutes)
    }
    
    @Test
    fun testAppointmentRequestSerialization() {
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 1
        )
        
        val serialized = json.encodeToString(AppointmentRequest.serializer(), appointmentRequest)
        val deserialized = json.decodeFromString(AppointmentRequest.serializer(), serialized)
        
        assertEquals(appointmentRequest.clientName, deserialized.clientName)
        assertEquals(appointmentRequest.clientEmail, deserialized.clientEmail)
        assertEquals(appointmentRequest.appointmentTime, deserialized.appointmentTime)
        assertEquals(appointmentRequest.serviceId, deserialized.serviceId)
    }
    
    @Test
    fun testApiResponseSerialization() {
        val apiResponse = ApiResponse(
            success = true,
            message = "Test successful",
            data = "Test data"
        )
        
        val serialized = json.encodeToString(ApiResponse.serializer(), apiResponse)
        val deserialized = json.decodeFromString<ApiResponse<String>>(serialized)
        
        assertEquals(apiResponse.success, deserialized.success)
        assertEquals(apiResponse.message, deserialized.message)
        assertEquals(apiResponse.data, deserialized.data)
    }
    
    @Test
    fun testApiResponseWithNullData() {
        val apiResponse = ApiResponse<String>(
            success = false,
            message = "Test failed",
            data = null
        )
        
        val serialized = json.encodeToString(ApiResponse.serializer(), apiResponse)
        val deserialized = json.decodeFromString<ApiResponse<String>>(serialized)
        
        assertEquals(apiResponse.success, deserialized.success)
        assertEquals(apiResponse.message, deserialized.message)
        assertNull(deserialized.data)
    }
    
    @Test
    fun testApiResponseWithComplexData() {
        val service = Service(
            id = 1,
            name = "Test Service",
            description = "A test service",
            defaultDurationInMinutes = 60
        )
        
        val apiResponse = ApiResponse(
            success = true,
            message = "Service retrieved successfully",
            data = service
        )
        
        val serialized = json.encodeToString(ApiResponse.serializer(), apiResponse)
        val deserialized = json.decodeFromString<ApiResponse<Service>>(serialized)
        
        assertEquals(apiResponse.success, deserialized.success)
        assertEquals(apiResponse.message, deserialized.message)
        assertNotNull(deserialized.data)
        assertEquals(service.id, deserialized.data?.id)
        assertEquals(service.name, deserialized.data?.name)
        assertEquals(service.description, deserialized.data?.description)
        assertEquals(service.defaultDurationInMinutes, deserialized.data?.defaultDurationInMinutes)
    }
    
    @Test
    fun testAppointmentResponseSerialization() {
        val appointment = Appointment(
            id = 1,
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 1,
            serviceName = "Test Service",
            durationInMinutes = 60
        )
        
        val appointmentResponse = AppointmentResponse(
            success = true,
            message = "Appointment created successfully",
            appointment = appointment
        )
        
        val serialized = json.encodeToString(AppointmentResponse.serializer(), appointmentResponse)
        val deserialized = json.decodeFromString(AppointmentResponse.serializer(), serialized)
        
        assertEquals(appointmentResponse.success, deserialized.success)
        assertEquals(appointmentResponse.message, deserialized.message)
        assertNotNull(deserialized.appointment)
        assertEquals(appointment.id, deserialized.appointment?.id)
        assertEquals(appointment.clientName, deserialized.appointment?.clientName)
        assertEquals(appointment.clientEmail, deserialized.appointment?.clientEmail)
        assertEquals(appointment.appointmentTime, deserialized.appointment?.appointmentTime)
        assertEquals(appointment.serviceId, deserialized.appointment?.serviceId)
        assertEquals(appointment.serviceName, deserialized.appointment?.serviceName)
        assertEquals(appointment.durationInMinutes, deserialized.appointment?.durationInMinutes)
    }
    
    @Test
    fun testServiceWithNullId() {
        val service = Service(
            id = null,
            name = "Test Service",
            description = "A test service",
            defaultDurationInMinutes = 60
        )
        
        val serialized = json.encodeToString(Service.serializer(), service)
        val deserialized = json.decodeFromString(Service.serializer(), serialized)
        
        assertNull(deserialized.id)
        assertEquals(service.name, deserialized.name)
        assertEquals(service.description, deserialized.description)
        assertEquals(service.defaultDurationInMinutes, deserialized.defaultDurationInMinutes)
    }
    
    @Test
    fun testAppointmentWithNullOptionalFields() {
        val appointment = Appointment(
            id = null,
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 1,
            serviceName = null,
            durationInMinutes = null
        )
        
        val serialized = json.encodeToString(Appointment.serializer(), appointment)
        val deserialized = json.decodeFromString(Appointment.serializer(), serialized)
        
        assertNull(deserialized.id)
        assertEquals(appointment.clientName, deserialized.clientName)
        assertEquals(appointment.clientEmail, deserialized.clientEmail)
        assertEquals(appointment.appointmentTime, deserialized.appointmentTime)
        assertEquals(appointment.serviceId, deserialized.serviceId)
        assertNull(deserialized.serviceName)
        assertNull(deserialized.durationInMinutes)
    }
    
    @Test
    fun testLocalDateTimeHandling() {
        val dateTime = LocalDateTime.parse("2024-12-01T10:30:45")
        
        val appointment = Appointment(
            id = 1,
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = dateTime,
            serviceId = 1,
            serviceName = "Test Service",
            durationInMinutes = 60
        )
        
        val serialized = json.encodeToString(Appointment.serializer(), appointment)
        val deserialized = json.decodeFromString(Appointment.serializer(), serialized)
        
        assertEquals(dateTime, deserialized.appointmentTime)
        assertEquals(dateTime.year, deserialized.appointmentTime.year)
        assertEquals(dateTime.monthNumber, deserialized.appointmentTime.monthNumber)
        assertEquals(dateTime.dayOfMonth, deserialized.appointmentTime.dayOfMonth)
        assertEquals(dateTime.hour, deserialized.appointmentTime.hour)
        assertEquals(dateTime.minute, deserialized.appointmentTime.minute)
        assertEquals(dateTime.second, deserialized.appointmentTime.second)
    }
    
    @Test
    fun testValidationConstraints() {
        // Test that models can handle edge cases
        val longName = "A".repeat(200)
        val longEmail = "test@${"a".repeat(200)}.com"
        val longDescription = "A".repeat(1000)
        
        val service = Service(
            id = 1,
            name = longName,
            description = longDescription,
            defaultDurationInMinutes = 1440 // 24 hours
        )
        
        val appointment = Appointment(
            id = 1,
            clientName = longName,
            clientEmail = longEmail,
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 1,
            serviceName = longName,
            durationInMinutes = 1440
        )
        
        // Should not throw exceptions during serialization
        assertDoesNotThrow {
            json.encodeToString(Service.serializer(), service)
            json.encodeToString(Appointment.serializer(), appointment)
        }
    }
}