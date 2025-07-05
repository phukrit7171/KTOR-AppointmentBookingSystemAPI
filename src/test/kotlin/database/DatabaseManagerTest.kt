package com.camt.database

import com.camt.models.AppointmentRequest
import com.camt.models.ServiceRequest
import com.camt.plugins.configureDatabase
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class DatabaseManagerTest {
    
    private lateinit var databaseManager: DatabaseManager
    
    @BeforeTest
    fun setup() {
        // Use in-memory H2 database for testing
        Database.connect(
            url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver"
        )
        
        transaction {
            SchemaUtils.create(Services, Appointments)
        }
        
        databaseManager = DatabaseManager()
    }
    
    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(Services, Appointments)
        }
    }
    
    // Service Tests
    @Test
    fun testGetAllServicesEmpty() {
        val services = databaseManager.getAllServices()
        assertTrue(services.isEmpty())
    }
    
    @Test
    fun testCreateService() {
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "A test service for unit testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        assertNotNull(createdService.id)
        assertEquals(serviceRequest.name, createdService.name)
        assertEquals(serviceRequest.description, createdService.description)
        assertEquals(serviceRequest.defaultDurationInMinutes, createdService.defaultDurationInMinutes)
    }
    
    @Test
    fun testGetServiceById() {
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "A test service for unit testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        val retrievedService = databaseManager.getServiceById(createdService.id!!)
        
        assertNotNull(retrievedService)
        assertEquals(createdService.id, retrievedService.id)
        assertEquals(createdService.name, retrievedService.name)
        assertEquals(createdService.description, retrievedService.description)
        assertEquals(createdService.defaultDurationInMinutes, retrievedService.defaultDurationInMinutes)
    }
    
    @Test
    fun testGetServiceByIdNotFound() {
        val service = databaseManager.getServiceById(999)
        assertNull(service)
    }
    
    @Test
    fun testUpdateService() {
        val originalRequest = ServiceRequest(
            name = "Original Service",
            description = "Original description",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(originalRequest)
        
        val updateRequest = ServiceRequest(
            name = "Updated Service",
            description = "Updated description",
            defaultDurationInMinutes = 90
        )
        
        val updatedService = databaseManager.updateService(createdService.id!!, updateRequest)
        
        assertNotNull(updatedService)
        assertEquals(createdService.id, updatedService.id)
        assertEquals(updateRequest.name, updatedService.name)
        assertEquals(updateRequest.description, updatedService.description)
        assertEquals(updateRequest.defaultDurationInMinutes, updatedService.defaultDurationInMinutes)
    }
    
    @Test
    fun testUpdateServiceNotFound() {
        val updateRequest = ServiceRequest(
            name = "Updated Service",
            description = "Updated description",
            defaultDurationInMinutes = 90
        )
        
        val result = databaseManager.updateService(999, updateRequest)
        assertNull(result)
    }
    
    @Test
    fun testDeleteService() {
        val serviceRequest = ServiceRequest(
            name = "Service to Delete",
            description = "Will be deleted",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        val deleted = databaseManager.deleteService(createdService.id!!)
        
        assertTrue(deleted)
        
        // Verify service is deleted
        val retrievedService = databaseManager.getServiceById(createdService.id!!)
        assertNull(retrievedService)
    }
    
    @Test
    fun testDeleteServiceWithAppointments() {
        // Create a service
        val serviceRequest = ServiceRequest(
            name = "Service with Appointments",
            description = "Cannot be deleted",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        // Create an appointment for this service
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        databaseManager.createAppointment(appointmentRequest)
        
        // Try to delete the service - should fail
        val deleted = databaseManager.deleteService(createdService.id!!)
        assertFalse(deleted)
        
        // Verify service still exists
        val retrievedService = databaseManager.getServiceById(createdService.id!!)
        assertNotNull(retrievedService)
    }
    
    @Test
    fun testGetAllServices() {
        // Create multiple services
        val service1 = ServiceRequest("Service 1", "Description 1", 30)
        val service2 = ServiceRequest("Service 2", "Description 2", 45)
        val service3 = ServiceRequest("Service 3", "Description 3", 60)
        
        databaseManager.createService(service1)
        databaseManager.createService(service2)
        databaseManager.createService(service3)
        
        val services = databaseManager.getAllServices()
        
        assertEquals(3, services.size)
        assertTrue(services.any { it.name == "Service 1" })
        assertTrue(services.any { it.name == "Service 2" })
        assertTrue(services.any { it.name == "Service 3" })
    }
    
    // Appointment Tests
    @Test
    fun testGetAllAppointmentsEmpty() {
        val appointments = databaseManager.getAllAppointments()
        assertTrue(appointments.isEmpty())
    }
    
    @Test
    fun testCreateAppointment() {
        // Create a service first
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        // Create an appointment
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val createdAppointment = databaseManager.createAppointment(appointmentRequest)
        
        assertNotNull(createdAppointment)
        assertNotNull(createdAppointment.id)
        assertEquals(appointmentRequest.clientName, createdAppointment.clientName)
        assertEquals(appointmentRequest.clientEmail, createdAppointment.clientEmail)
        assertEquals(appointmentRequest.appointmentTime, createdAppointment.appointmentTime)
        assertEquals(appointmentRequest.serviceId, createdAppointment.serviceId)
        assertEquals(createdService.name, createdAppointment.serviceName)
        assertEquals(createdService.defaultDurationInMinutes, createdAppointment.durationInMinutes)
    }
    
    @Test
    fun testCreateAppointmentWithInvalidService() {
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 999 // Non-existent service
        )
        
        val result = databaseManager.createAppointment(appointmentRequest)
        assertNull(result)
    }
    
    @Test
    fun testCreateAppointmentDoubleBooking() {
        // Create a service
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For double booking test",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        // Create first appointment
        val appointmentRequest1 = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val appointment1 = databaseManager.createAppointment(appointmentRequest1)
        assertNotNull(appointment1)
        
        // Try to create overlapping appointment (should fail)
        val appointmentRequest2 = AppointmentRequest(
            clientName = "Jane Doe",
            clientEmail = "jane@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:30:00"), // Overlaps with first appointment
            serviceId = createdService.id!!
        )
        
        val appointment2 = databaseManager.createAppointment(appointmentRequest2)
        assertNull(appointment2)
    }
    
    @Test
    fun testCreateAppointmentNoOverlap() {
        // Create a service
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For no overlap test",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        // Create first appointment
        val appointmentRequest1 = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val appointment1 = databaseManager.createAppointment(appointmentRequest1)
        assertNotNull(appointment1)
        
        // Create non-overlapping appointment (should succeed)
        val appointmentRequest2 = AppointmentRequest(
            clientName = "Jane Doe",
            clientEmail = "jane@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T11:00:00"), // No overlap
            serviceId = createdService.id!!
        )
        
        val appointment2 = databaseManager.createAppointment(appointmentRequest2)
        assertNotNull(appointment2)
    }
    
    @Test
    fun testGetAppointmentById() {
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val createdAppointment = databaseManager.createAppointment(appointmentRequest)!!
        val retrievedAppointment = databaseManager.getAppointmentById(createdAppointment.id!!)
        
        assertNotNull(retrievedAppointment)
        assertEquals(createdAppointment.id, retrievedAppointment.id)
        assertEquals(createdAppointment.clientName, retrievedAppointment.clientName)
        assertEquals(createdAppointment.clientEmail, retrievedAppointment.clientEmail)
        assertEquals(createdAppointment.appointmentTime, retrievedAppointment.appointmentTime)
        assertEquals(createdAppointment.serviceId, retrievedAppointment.serviceId)
        assertEquals(createdAppointment.serviceName, retrievedAppointment.serviceName)
        assertEquals(createdAppointment.durationInMinutes, retrievedAppointment.durationInMinutes)
    }
    
    @Test
    fun testGetAppointmentByIdNotFound() {
        val appointment = databaseManager.getAppointmentById(999)
        assertNull(appointment)
    }
    
    @Test
    fun testUpdateAppointment() {
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val createdAppointment = databaseManager.createAppointment(appointmentRequest)!!
        
        // Update appointment
        val updateRequest = AppointmentRequest(
            clientName = "John Updated",
            clientEmail = "john.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T14:00:00"),
            serviceId = createdService.id!!
        )
        
        val updatedAppointment = databaseManager.updateAppointment(createdAppointment.id!!, updateRequest)
        
        assertNotNull(updatedAppointment)
        assertEquals(createdAppointment.id, updatedAppointment.id)
        assertEquals(updateRequest.clientName, updatedAppointment.clientName)
        assertEquals(updateRequest.clientEmail, updatedAppointment.clientEmail)
        assertEquals(updateRequest.appointmentTime, updatedAppointment.appointmentTime)
        assertEquals(updateRequest.serviceId, updatedAppointment.serviceId)
    }
    
    @Test
    fun testUpdateAppointmentWithInvalidService() {
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val createdAppointment = databaseManager.createAppointment(appointmentRequest)!!
        
        // Try to update with invalid service
        val updateRequest = AppointmentRequest(
            clientName = "John Updated",
            clientEmail = "john.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T14:00:00"),
            serviceId = 999 // Non-existent service
        )
        
        val result = databaseManager.updateAppointment(createdAppointment.id!!, updateRequest)
        assertNull(result)
    }
    
    @Test
    fun testUpdateAppointmentDoubleBooking() {
        // Create service
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For double booking test",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        // Create two appointments
        val appointmentRequest1 = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val appointmentRequest2 = AppointmentRequest(
            clientName = "Jane Doe",
            clientEmail = "jane@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T12:00:00"),
            serviceId = createdService.id!!
        )
        
        val appointment1 = databaseManager.createAppointment(appointmentRequest1)!!
        val appointment2 = databaseManager.createAppointment(appointmentRequest2)!!
        
        // Try to update appointment2 to overlap with appointment1
        val updateRequest = AppointmentRequest(
            clientName = "Jane Updated",
            clientEmail = "jane.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:30:00"), // Overlaps with appointment1
            serviceId = createdService.id!!
        )
        
        val result = databaseManager.updateAppointment(appointment2.id!!, updateRequest)
        assertNull(result)
    }
    
    @Test
    fun testUpdateAppointmentSameTimeSlot() {
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val createdAppointment = databaseManager.createAppointment(appointmentRequest)!!
        
        // Update appointment to same time slot (should succeed)
        val updateRequest = AppointmentRequest(
            clientName = "John Updated",
            clientEmail = "john.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"), // Same time
            serviceId = createdService.id!!
        )
        
        val updatedAppointment = databaseManager.updateAppointment(createdAppointment.id!!, updateRequest)
        
        assertNotNull(updatedAppointment)
        assertEquals(updateRequest.clientName, updatedAppointment.clientName)
        assertEquals(updateRequest.clientEmail, updatedAppointment.clientEmail)
    }
    
    @Test
    fun testDeleteAppointment() {
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val createdAppointment = databaseManager.createAppointment(appointmentRequest)!!
        val deleted = databaseManager.deleteAppointment(createdAppointment.id!!)
        
        assertTrue(deleted)
        
        // Verify appointment is deleted
        val retrievedAppointment = databaseManager.getAppointmentById(createdAppointment.id!!)
        assertNull(retrievedAppointment)
    }
    
    @Test
    fun testDeleteAppointmentNotFound() {
        val deleted = databaseManager.deleteAppointment(999)
        assertFalse(deleted)
    }
    
    @Test
    fun testGetAllAppointments() {
        // Create a service
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For multiple appointments",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        // Create multiple appointments
        val appointment1 = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        val appointment2 = AppointmentRequest(
            clientName = "Jane Doe",
            clientEmail = "jane@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T12:00:00"),
            serviceId = createdService.id!!
        )
        
        val appointment3 = AppointmentRequest(
            clientName = "Bob Smith",
            clientEmail = "bob@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T14:00:00"),
            serviceId = createdService.id!!
        )
        
        databaseManager.createAppointment(appointment1)
        databaseManager.createAppointment(appointment2)
        databaseManager.createAppointment(appointment3)
        
        val appointments = databaseManager.getAllAppointments()
        
        assertEquals(3, appointments.size)
        assertTrue(appointments.any { it.clientName == "John Doe" })
        assertTrue(appointments.any { it.clientName == "Jane Doe" })
        assertTrue(appointments.any { it.clientName == "Bob Smith" })
        
        // Verify appointments are ordered by time
        assertEquals("John Doe", appointments[0].clientName)
        assertEquals("Jane Doe", appointments[1].clientName)
        assertEquals("Bob Smith", appointments[2].clientName)
    }
    
    @Test
    fun testOverlapDetectionEdgeCases() {
        // Create service
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For overlap testing",
            defaultDurationInMinutes = 60
        )
        
        val createdService = databaseManager.createService(serviceRequest)
        
        // Create base appointment (10:00 - 11:00)
        val baseAppointment = AppointmentRequest(
            clientName = "Base Appointment",
            clientEmail = "base@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        databaseManager.createAppointment(baseAppointment)
        
        // Test exact start time conflict
        val conflictStart = AppointmentRequest(
            clientName = "Conflict Start",
            clientEmail = "conflict.start@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = createdService.id!!
        )
        
        assertNull(databaseManager.createAppointment(conflictStart))
        
        // Test exact end time should be allowed (11:00 - 12:00)
        val afterEnd = AppointmentRequest(
            clientName = "After End",
            clientEmail = "after.end@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T11:00:00"),
            serviceId = createdService.id!!
        )
        
        assertNotNull(databaseManager.createAppointment(afterEnd))
        
        // Test before start should be allowed (09:00 - 10:00)
        val beforeStart = AppointmentRequest(
            clientName = "Before Start",
            clientEmail = "before.start@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T09:00:00"),
            serviceId = createdService.id!!
        )
        
        assertNotNull(databaseManager.createAppointment(beforeStart))
    }
}