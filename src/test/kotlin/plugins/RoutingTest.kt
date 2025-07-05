package com.camt.plugins

import com.camt.database.DatabaseManager
import com.camt.models.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.*

class RoutingTest {

    @Test
    fun testRootRoute() = testApplication {
        application {
            configureRouting()
        }
        
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello Mr.Phukrit Kittinontana", bodyAsText())
        }
    }

    @Test
    fun testGetAllServicesEmpty() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        client.get("/api/services").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<List<Service>>>()
            assertTrue(response.success)
            assertEquals("Services retrieved successfully", response.message)
            assertTrue(response.data?.isEmpty() == true)
        }
    }

    @Test
    fun testCreateService() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "A test service",
            defaultDurationInMinutes = 60
        )
        
        client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ApiResponse<Service>>()
            assertTrue(response.success)
            assertEquals("Service created successfully", response.message)
            assertNotNull(response.data)
            assertEquals(serviceRequest.name, response.data?.name)
            assertEquals(serviceRequest.description, response.data?.description)
            assertEquals(serviceRequest.defaultDurationInMinutes, response.data?.defaultDurationInMinutes)
            assertNotNull(response.data?.id)
        }
    }

    @Test
    fun testGetServiceById() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // First create a service
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "A test service",
            defaultDurationInMinutes = 60
        )
        
        val createResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createResponse.data?.id!!
        
        // Now get the service by ID
        client.get("/api/services/$serviceId").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<Service>>()
            assertTrue(response.success)
            assertEquals("Service retrieved successfully", response.message)
            assertEquals(serviceId, response.data?.id)
            assertEquals(serviceRequest.name, response.data?.name)
        }
    }

    @Test
    fun testGetServiceByIdNotFound() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        client.get("/api/services/999").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            val response = body<ApiResponse<Service>>()
            assertFalse(response.success)
            assertEquals("Service not found", response.message)
            assertNull(response.data)
        }
    }

    @Test
    fun testGetServiceByIdInvalidId() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        client.get("/api/services/invalid").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val response = body<ApiResponse<Service>>()
            assertFalse(response.success)
            assertEquals("Invalid service ID", response.message)
        }
    }

    @Test
    fun testUpdateService() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create a service first
        val originalService = ServiceRequest(
            name = "Original Service",
            description = "Original description",
            defaultDurationInMinutes = 60
        )
        
        val createResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(originalService)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createResponse.data?.id!!
        
        // Update the service
        val updateRequest = ServiceRequest(
            name = "Updated Service",
            description = "Updated description",
            defaultDurationInMinutes = 90
        )
        
        client.put("/api/services/$serviceId") {
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<Service>>()
            assertTrue(response.success)
            assertEquals("Service updated successfully", response.message)
            assertEquals(serviceId, response.data?.id)
            assertEquals(updateRequest.name, response.data?.name)
            assertEquals(updateRequest.description, response.data?.description)
            assertEquals(updateRequest.defaultDurationInMinutes, response.data?.defaultDurationInMinutes)
        }
    }

    @Test
    fun testUpdateServiceNotFound() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        val updateRequest = ServiceRequest(
            name = "Updated Service",
            description = "Updated description",
            defaultDurationInMinutes = 90
        )
        
        client.put("/api/services/999") {
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)
            val response = body<ApiResponse<Service>>()
            assertFalse(response.success)
            assertEquals("Service not found", response.message)
        }
    }

    @Test
    fun testDeleteService() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create a service first
        val serviceRequest = ServiceRequest(
            name = "Service to Delete",
            description = "Will be deleted",
            defaultDurationInMinutes = 60
        )
        
        val createResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createResponse.data?.id!!
        
        // Delete the service
        client.delete("/api/services/$serviceId").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<String>>()
            assertTrue(response.success)
            assertEquals("Service deleted successfully", response.message)
            assertEquals("Service deleted", response.data)
        }
    }

    @Test
    fun testDeleteServiceWithAppointments() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create a service first
        val serviceRequest = ServiceRequest(
            name = "Service with Appointments",
            description = "Cannot be deleted",
            defaultDurationInMinutes = 60
        )
        
        val createServiceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createServiceResponse.data?.id!!
        
        // Create an appointment for this service
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }
        
        // Try to delete the service - should fail
        client.delete("/api/services/$serviceId").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val response = body<ApiResponse<String>>()
            assertFalse(response.success)
            assertEquals("Cannot delete service with existing appointments", response.message)
        }
    }

    @Test
    fun testGetAllAppointmentsEmpty() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        client.get("/api/appointments").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<List<Appointment>>>()
            assertTrue(response.success)
            assertEquals("Appointments retrieved successfully", response.message)
            assertTrue(response.data?.isEmpty() == true)
        }
    }

    @Test
    fun testCreateAppointment() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create a service first
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment",
            defaultDurationInMinutes = 60
        )
        
        val createServiceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createServiceResponse.data?.id!!
        
        // Create an appointment
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ApiResponse<Appointment>>()
            assertTrue(response.success)
            assertEquals("Appointment created successfully", response.message)
            assertNotNull(response.data)
            assertEquals(appointmentRequest.clientName, response.data?.clientName)
            assertEquals(appointmentRequest.clientEmail, response.data?.clientEmail)
            assertEquals(appointmentRequest.serviceId, response.data?.serviceId)
            assertEquals(serviceRequest.name, response.data?.serviceName)
            assertEquals(serviceRequest.defaultDurationInMinutes, response.data?.durationInMinutes)
        }
    }

    @Test
    fun testCreateAppointmentWithInvalidService() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = 999 // Non-existent service
        )
        
        client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }.apply {
            assertEquals(HttpStatusCode.Conflict, status)
            val response = body<ApiResponse<Appointment>>()
            assertFalse(response.success)
            assertEquals("Cannot create appointment: Service not found or time slot already booked", response.message)
        }
    }

    @Test
    fun testCreateAppointmentDoubleBooking() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create a service first
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For double booking test",
            defaultDurationInMinutes = 60
        )
        
        val createServiceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createServiceResponse.data?.id!!
        
        // Create first appointment
        val appointmentRequest1 = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest1)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        
        // Try to create overlapping appointment
        val appointmentRequest2 = AppointmentRequest(
            clientName = "Jane Doe",
            clientEmail = "jane@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:30:00"), // Overlaps with first appointment
            serviceId = serviceId
        )
        
        client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest2)
        }.apply {
            assertEquals(HttpStatusCode.Conflict, status)
            val response = body<ApiResponse<Appointment>>()
            assertFalse(response.success)
            assertEquals("Cannot create appointment: Service not found or time slot already booked", response.message)
        }
    }

    @Test
    fun testGetAppointmentById() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment",
            defaultDurationInMinutes = 60
        )
        
        val createServiceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createServiceResponse.data?.id!!
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val createAppointmentResponse = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }.body<ApiResponse<Appointment>>()
        
        val appointmentId = createAppointmentResponse.data?.id!!
        
        // Get appointment by ID
        client.get("/api/appointments/$appointmentId").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<Appointment>>()
            assertTrue(response.success)
            assertEquals("Appointment retrieved successfully", response.message)
            assertEquals(appointmentId, response.data?.id)
            assertEquals(appointmentRequest.clientName, response.data?.clientName)
        }
    }

    @Test
    fun testGetAppointmentByIdNotFound() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        client.get("/api/appointments/999").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            val response = body<ApiResponse<Appointment>>()
            assertFalse(response.success)
            assertEquals("Appointment not found", response.message)
        }
    }

    @Test
    fun testUpdateAppointment() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment",
            defaultDurationInMinutes = 60
        )
        
        val createServiceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createServiceResponse.data?.id!!
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val createAppointmentResponse = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }.body<ApiResponse<Appointment>>()
        
        val appointmentId = createAppointmentResponse.data?.id!!
        
        // Update appointment
        val updateRequest = AppointmentRequest(
            clientName = "John Updated",
            clientEmail = "john.updated@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T14:00:00"),
            serviceId = serviceId
        )
        
        client.put("/api/appointments/$appointmentId") {
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<Appointment>>()
            assertTrue(response.success)
            assertEquals("Appointment updated successfully", response.message)
            assertEquals(appointmentId, response.data?.id)
            assertEquals(updateRequest.clientName, response.data?.clientName)
            assertEquals(updateRequest.clientEmail, response.data?.clientEmail)
        }
    }

    @Test
    fun testDeleteAppointment() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        // Create service and appointment
        val serviceRequest = ServiceRequest(
            name = "Test Service",
            description = "For appointment",
            defaultDurationInMinutes = 60
        )
        
        val createServiceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = createServiceResponse.data?.id!!
        
        val appointmentRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        val createAppointmentResponse = client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(appointmentRequest)
        }.body<ApiResponse<Appointment>>()
        
        val appointmentId = createAppointmentResponse.data?.id!!
        
        // Delete appointment
        client.delete("/api/appointments/$appointmentId").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ApiResponse<String>>()
            assertTrue(response.success)
            assertEquals("Appointment deleted successfully", response.message)
            assertEquals("Appointment deleted", response.data)
        }
    }

    @Test
    fun testDeleteAppointmentNotFound() = testApplication {
        application {
            configureDatabase()
            configureSerialization()
            configureRouting()
        }
        
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        
        client.delete("/api/appointments/999").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            val response = body<ApiResponse<String>>()
            assertFalse(response.success)
            assertEquals("Appointment not found", response.message)
        }
    }
}