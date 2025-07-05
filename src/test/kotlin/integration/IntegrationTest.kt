package com.camt.integration

import com.camt.TestConfiguration
import com.camt.models.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDateTime
import kotlin.test.*
import com.camt.configureTestApplication

class IntegrationTest {
    
    @Test
    fun testCompleteServiceLifecycle() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // 1. Start with empty services
        val initialServices = client.get("/api/services").body<ApiResponse<List<Service>>>()
        assertTrue(initialServices.success)
        assertTrue(initialServices.data?.isEmpty() == true)
        
        // 2. Create a service
        val serviceRequest = ServiceRequest(
            name = "Haircut",
            description = "Professional haircut service",
            defaultDurationInMinutes = 60
        )
        
        val createResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        assertTrue(createResponse.success)
        assertNotNull(createResponse.data?.id)
        val serviceId = createResponse.data?.id!!
        
        // 3. Verify service can be retrieved
        val getResponse = client.get("/api/services/$serviceId").body<ApiResponse<Service>>()
        assertTrue(getResponse.success)
        assertEquals(serviceRequest.name, getResponse.data?.name)
        
        // 4. Update the service
        val updateRequest = ServiceRequest(
            name = "Premium Haircut",
            description = "Premium professional haircut service",
            defaultDurationInMinutes = 90
        )
        
        val updateResponse = client.put("/api/services/$serviceId") {
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }.body<ApiResponse<Service>>()
        
        assertTrue(updateResponse.success)
        assertEquals(updateRequest.name, updateResponse.data?.name)
        assertEquals(updateRequest.defaultDurationInMinutes, updateResponse.data?.defaultDurationInMinutes)
        
        // 5. Verify all services includes the updated service
        val allServicesResponse = client.get("/api/services").body<ApiResponse<List<Service>>>()
        assertTrue(allServicesResponse.success)
        assertEquals(1, allServicesResponse.data?.size)
        assertEquals(updateRequest.name, allServicesResponse.data?.get(0)?.name)
        
        // 6. Delete the service (should succeed since no appointments exist)
        val deleteResponse = client.delete("/api/services/$serviceId").body<ApiResponse<String>>()
        assertTrue(deleteResponse.success)
        
        // 7. Verify service is deleted
        val finalServices = client.get("/api/services").body<ApiResponse<List<Service>>>()
        assertTrue(finalServices.success)
        assertTrue(finalServices.data?.isEmpty() == true)
    }
    
    @Test
    fun testCompleteAppointmentLifecycle() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // 1. Create a service first
        val serviceRequest = ServiceRequest(
            name = "Haircut",
            description = "Professional haircut service",
            defaultDurationInMinutes = 60
        )
        
        val serviceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = serviceResponse.data?.id!!
        
        // 2. Start with empty appointments
        val initialAppointments = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
        assertTrue(initialAppointments.success)
        assertTrue(initialAppointments.data?.isEmpty() == true)
        
        // 3. Create an appointment
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val createResponse = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(createResponse.success)
        assertNotNull(createResponse.data?.id)
        val appointmentId = createResponse.data?.id!!
        assertEquals(serviceRequest.name, createResponse.data?.serviceName)
        
        // 4. Verify appointment can be retrieved
        val getResponse = client.get("/api/appointments/$appointmentId").body<ApiResponse<Appointment>>()
        assertTrue(getResponse.success)
        assertEquals(appointmentRequest.clientName, getResponse.data?.clientName)
        assertEquals(appointmentRequest.clientEmail, getResponse.data?.clientEmail)
        
        // 5. Update the appointment
        val updateRequest = AppointmentRequest(
            clientName = "John Smith",
            clientEmail = "john.smith@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T14:00:00"),
            serviceId = serviceId
        )
        
        val updateResponse = client.put("/api/appointments/$appointmentId") {
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(updateResponse.success)
        assertEquals(updateRequest.clientName, updateResponse.data?.clientName)
        assertEquals(updateRequest.clientEmail, updateResponse.data?.clientEmail)
        
        // 6. Verify all appointments includes the updated appointment
        val allAppointmentsResponse = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
        assertTrue(allAppointmentsResponse.success)
        assertEquals(1, allAppointmentsResponse.data?.size)
        assertEquals(updateRequest.clientName, allAppointmentsResponse.data?.get(0)?.clientName)
        
        // 7. Delete the appointment
        val deleteResponse = client.delete("/api/appointments/$appointmentId").body<ApiResponse<String>>()
        assertTrue(deleteResponse.success)
        
        // 8. Verify appointment is deleted
        val finalAppointments = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
        assertTrue(finalAppointments.success)
        assertTrue(finalAppointments.data?.isEmpty() == true)
    }
    
    @Test
    fun testServiceAppointmentRelationship() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // 1. Create a service
        val serviceRequest = ServiceRequest(
            name = "Consultation",
            description = "Professional consultation service",
            defaultDurationInMinutes = 45
        )
        
        val serviceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = serviceResponse.data?.id!!
        
        // 2. Create an appointment for the service
        val appointmentRequest = AppointmentRequest(
            clientName = "Jane Doe",
            clientEmail = "jane@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val appointmentResponse = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(appointmentResponse.success)
        val appointmentId = appointmentResponse.data?.id!!
        
        // 3. Verify appointment has correct service information
        val getAppointmentResponse = client.get("/api/appointments/$appointmentId").body<ApiResponse<Appointment>>()
        assertTrue(getAppointmentResponse.success)
        assertEquals(serviceId, getAppointmentResponse.data?.serviceId)
        assertEquals(serviceRequest.name, getAppointmentResponse.data?.serviceName)
        assertEquals(serviceRequest.defaultDurationInMinutes, getAppointmentResponse.data?.durationInMinutes)
        
        // 4. Try to delete service with existing appointment (should fail)
        val deleteServiceResponse = client.delete("/api/services/$serviceId")
        assertEquals(HttpStatusCode.BadRequest, deleteServiceResponse.status)
        
        val deleteServiceBody = deleteServiceResponse.body<ApiResponse<String>>()
        assertFalse(deleteServiceBody.success)
        assertEquals("Cannot delete service with existing appointments", deleteServiceBody.message)
        
        // 5. Delete the appointment first
        val deleteAppointmentResponse = client.delete("/api/appointments/$appointmentId").body<ApiResponse<String>>()
        assertTrue(deleteAppointmentResponse.success)
        
        // 6. Now delete the service (should succeed)
        val deleteServiceResponse2 = client.delete("/api/services/$serviceId").body<ApiResponse<String>>()
        assertTrue(deleteServiceResponse2.success)
    }
    
    @Test
    fun testDoubleBookingPrevention() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // 1. Create a service
        val serviceRequest = ServiceRequest(
            name = "Massage",
            description = "Relaxing massage service",
            defaultDurationInMinutes = 90
        )
        
        val serviceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = serviceResponse.data?.id!!
        
        // 2. Create first appointment (10:00 - 11:30)
        val appointment1 = AppointmentRequest(
            clientName = "Client 1",
            clientEmail = "client1@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val response1 = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointment1)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(response1.success)
        val appointmentId1 = response1.data?.id!!
        
        // 3. Try to create overlapping appointment (10:30 - 12:00) - should fail
        val appointment2 = AppointmentRequest(
            clientName = "Client 2",
            clientEmail = "client2@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:30:00"),
            serviceId = serviceId
        )
        
        val response2 = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointment2)
        }
        
        assertEquals(HttpStatusCode.Conflict, response2.status)
        val conflictBody = response2.body<ApiResponse<Appointment>>()
        assertFalse(conflictBody.success)
        
        // 4. Create non-overlapping appointment (11:30 - 13:00) - should succeed
        val appointment3 = AppointmentRequest(
            clientName = "Client 3",
            clientEmail = "client3@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T11:30:00"),
            serviceId = serviceId
        )
        
        val response3 = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointment3)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(response3.success)
        val appointmentId3 = response3.data?.id!!
        
        // 5. Create appointment before first one (08:30 - 10:00) - should succeed
        val appointment4 = AppointmentRequest(
            clientName = "Client 4",
            clientEmail = "client4@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T08:30:00"),
            serviceId = serviceId
        )
        
        val response4 = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointment4)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(response4.success)
        
        // 6. Verify all appointments are ordered correctly
        val allAppointments = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
        assertTrue(allAppointments.success)
        assertEquals(3, allAppointments.data?.size)
        
        val appointments = allAppointments.data!!
        assertEquals("Client 4", appointments[0].clientName) // 08:30
        assertEquals("Client 1", appointments[1].clientName) // 10:00
        assertEquals("Client 3", appointments[2].clientName) // 11:30
    }
    
    @Test
    fun testAppointmentUpdateConflictPrevention() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // 1. Create a service
        val serviceRequest = ServiceRequest(
            name = "Therapy Session",
            description = "Therapy session service",
            defaultDurationInMinutes = 60
        )
        
        val serviceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = serviceResponse.data?.id!!
        
        // 2. Create two appointments
        val appointment1 = AppointmentRequest(
            clientName = "Client 1",
            clientEmail = "client1@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val appointment2 = AppointmentRequest(
            clientName = "Client 2",
            clientEmail = "client2@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T12:00:00"),
            serviceId = serviceId
        )
        
        val response1 = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointment1)
        }.body<ApiResponse<Appointment>>()
        
        val response2 = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointment2)
        }.body<ApiResponse<Appointment>>()
        
        val appointmentId1 = response1.data?.id!!
        val appointmentId2 = response2.data?.id!!
        
        // 3. Try to update appointment2 to conflict with appointment1 - should fail
        val conflictingUpdate = AppointmentRequest(
            clientName = "Client 2 Updated",
            clientEmail = "client2.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:30:00"), // Conflicts with appointment1
            serviceId = serviceId
        )
        
        val updateResponse = client.put("/api/appointments/$appointmentId2") {
            contentType(ContentType.Application.Json)
            setBody(conflictingUpdate)
        }
        
        assertEquals(HttpStatusCode.Conflict, updateResponse.status)
        val conflictBody = updateResponse.body<ApiResponse<Appointment>>()
        assertFalse(conflictBody.success)
        
        // 4. Update appointment2 to non-conflicting time - should succeed
        val validUpdate = AppointmentRequest(
            clientName = "Client 2 Updated",
            clientEmail = "client2.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T14:00:00"),
            serviceId = serviceId
        )
        
        val validUpdateResponse = client.put("/api/appointments/$appointmentId2") {
            contentType(ContentType.Application.Json)
            setBody(validUpdate)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(validUpdateResponse.success)
        assertEquals(validUpdate.clientName, validUpdateResponse.data?.clientName)
        assertEquals(validUpdate.appointmentTime, validUpdateResponse.data?.appointmentTime)
        
        // 5. Update appointment1 to same time slot - should succeed (same appointment)
        val sameTimeUpdate = AppointmentRequest(
            clientName = "Client 1 Updated",
            clientEmail = "client1.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"), // Same time
            serviceId = serviceId
        )
        
        val sameTimeResponse = client.put("/api/appointments/$appointmentId1") {
            contentType(ContentType.Application.Json)
            setBody(sameTimeUpdate)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(sameTimeResponse.success)
        assertEquals(sameTimeUpdate.clientName, sameTimeResponse.data?.clientName)
    }
    
    @Test
    fun testMultipleServicesAndAppointments() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // 1. Create multiple services
        val services = listOf(
            ServiceRequest("Haircut", "Professional haircut", 60),
            ServiceRequest("Massage", "Relaxing massage", 90),
            ServiceRequest("Consultation", "Professional consultation", 45)
        )
        
        val createdServices = services.map { service ->
            client.post("/api/services") {
                contentType(ContentType.Application.Json)
                setBody(service)
            }.body<ApiResponse<Service>>()
        }
        
        assertTrue(createdServices.all { it.success })
        val serviceIds = createdServices.mapNotNull { it.data?.id }
        assertEquals(3, serviceIds.size)
        
        // 2. Create appointments for different services
        val appointments = listOf(
            AppointmentRequest("John Doe", "john@example.com", LocalDateTime.parse("2024-12-01T10:00:00"), serviceIds[0]),
            AppointmentRequest("Jane Smith", "jane@example.com", LocalDateTime.parse("2024-12-01T11:00:00"), serviceIds[1]),
            AppointmentRequest("Bob Johnson", "bob@example.com", LocalDateTime.parse("2024-12-01T12:00:00"), serviceIds[2]),
            AppointmentRequest("Alice Brown", "alice@example.com", LocalDateTime.parse("2024-12-01T13:00:00"), serviceIds[0])
        )
        
        val createdAppointments = appointments.map { appointment ->
            client.post("/api/appointments") {
                contentType(ContentType.Application.Json)
                setBody(appointment)
            }.body<ApiResponse<Appointment>>()
        }
        
        assertTrue(createdAppointments.all { it.success })
        
        // 3. Verify all appointments are created and ordered correctly
        val allAppointments = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
        assertTrue(allAppointments.success)
        assertEquals(4, allAppointments.data?.size)
        
        val appointmentData = allAppointments.data!!
        assertEquals("John Doe", appointmentData[0].clientName)
        assertEquals("Jane Smith", appointmentData[1].clientName)
        assertEquals("Bob Johnson", appointmentData[2].clientName)
        assertEquals("Alice Brown", appointmentData[3].clientName)
        
        // 4. Verify service names are correctly populated
        assertEquals("Haircut", appointmentData[0].serviceName)
        assertEquals("Massage", appointmentData[1].serviceName)
        assertEquals("Consultation", appointmentData[2].serviceName)
        assertEquals("Haircut", appointmentData[3].serviceName)
        
        // 5. Verify all services are still retrievable
        val allServices = client.get("/api/services").body<ApiResponse<List<Service>>>()
        assertTrue(allServices.success)
        assertEquals(3, allServices.data?.size)
    }
    
    @Test
    fun testErrorHandlingAndRecovery() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // 1. Test invalid service ID in appointment creation
        val invalidServiceAppointment = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 999
        )
        
        val invalidResponse = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(invalidServiceAppointment)
        }
        
        assertEquals(HttpStatusCode.Conflict, invalidResponse.status)
        
        // 2. Create valid service and appointment
        val serviceRequest = ServiceRequest(
            name = "Valid Service",
            description = "A valid service",
            defaultDurationInMinutes = 60
        )
        
        val serviceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        assertTrue(serviceResponse.success)
        val serviceId = serviceResponse.data?.id!!
        
        val validAppointment = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val validResponse = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(validAppointment)
        }.body<ApiResponse<Appointment>>()
        
        assertTrue(validResponse.success)
        
        // 3. Test various invalid ID formats
        val invalidIds = listOf("invalid", "abc", "-1", "0", "999999")
        
        invalidIds.forEach { invalidId ->
            val serviceResponse = client.get("/api/services/$invalidId")
            assertEquals(HttpStatusCode.BadRequest, serviceResponse.status)
            
            val appointmentResponse = client.get("/api/appointments/$invalidId")
            assertEquals(HttpStatusCode.BadRequest, appointmentResponse.status)
        }
        
        // 4. Test non-existent resources
        val nonExistentIds = listOf("999", "1000", "888")
        
        nonExistentIds.forEach { id ->
            val serviceResponse = client.get("/api/services/$id")
            assertEquals(HttpStatusCode.NotFound, serviceResponse.status)
            
            val appointmentResponse = client.get("/api/appointments/$id")
            assertEquals(HttpStatusCode.NotFound, appointmentResponse.status)
        }
    }
}