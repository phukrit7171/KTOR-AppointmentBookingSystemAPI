package com.camt.performance

import com.camt.TestConfiguration
import com.camt.models.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlin.test.*
import kotlin.time.measureTime
import com.camt.configureTestApplication

class PerformanceTest {
    
    @Test
    fun testConcurrentServiceCreation() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        val numberOfServices = 50
        val services = (1..numberOfServices).map { i ->
            ServiceRequest(
                name = "Service $i",
                description = "Performance test service $i",
                defaultDurationInMinutes = 60
            )
        }
        
        val executionTime = measureTime {
            runBlocking {
                val results = services.map { service ->
                    async {
                        client.post("/api/services") {
                            contentType(ContentType.Application.Json)
                            setBody(service)
                        }.body<ApiResponse<Service>>()
                    }
                }.awaitAll()
                
                // Verify all services were created successfully
                assertTrue(results.all { it.success })
                assertEquals(numberOfServices, results.size)
            }
        }
        
        println("Created $numberOfServices services in ${executionTime.inWholeMilliseconds}ms")
        
        // Verify all services can be retrieved
        val allServices = client.get("/api/services").body<ApiResponse<List<Service>>>()
        assertTrue(allServices.success)
        assertEquals(numberOfServices, allServices.data?.size)
    }
    
    @Test
    fun testConcurrentAppointmentCreation() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // Create a service first
        val serviceRequest = ServiceRequest(
            name = "Performance Test Service",
            description = "Service for performance testing",
            defaultDurationInMinutes = 30 // Shorter duration to allow more appointments
        )
        
        val serviceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = serviceResponse.data?.id!!
        
        // Create non-overlapping appointments
        val numberOfAppointments = 20
        val appointments = (1..numberOfAppointments).map { i ->
            AppointmentRequest(
                clientName = "Client $i",
                clientEmail = "client$i@example.com",
                appointmentTime = LocalDateTime.parse("2024-12-01T${(8 + i).toString().padStart(2, '0')}:00:00"),
                serviceId = serviceId
            )
        }
        
        val executionTime = measureTime {
            runBlocking {
                val results = appointments.map { appointment ->
                    async {
                        client.post("/api/appointments") {
                            contentType(ContentType.Application.Json)
                            setBody(appointment)
                        }.body<ApiResponse<Appointment>>()
                    }
                }.awaitAll()
                
                // Verify all appointments were created successfully
                assertTrue(results.all { it.success })
                assertEquals(numberOfAppointments, results.size)
            }
        }
        
        println("Created $numberOfAppointments appointments in ${executionTime.inWholeMilliseconds}ms")
        
        // Verify all appointments can be retrieved
        val allAppointments = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
        assertTrue(allAppointments.success)
        assertEquals(numberOfAppointments, allAppointments.data?.size)
    }
    
    @Test
    fun testLargeDatasetRetrieval() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // Create multiple services
        val numberOfServices = 10
        val services = (1..numberOfServices).map { i ->
            ServiceRequest(
                name = "Service $i",
                description = "Large dataset test service $i",
                defaultDurationInMinutes = 60
            )
        }
        
        val createdServices = services.map { service ->
            client.post("/api/services") {
                contentType(ContentType.Application.Json)
                setBody(service)
            }.body<ApiResponse<Service>>()
        }
        
        val serviceIds = createdServices.mapNotNull { it.data?.id }
        
        // Create multiple appointments for each service
        val appointmentsPerService = 10
        val appointments = serviceIds.flatMap { serviceId ->
            (1..appointmentsPerService).map { i ->
                AppointmentRequest(
                    clientName = "Client $serviceId-$i",
                    clientEmail = "client$serviceId-$i@example.com",
                    appointmentTime = LocalDateTime.parse("2024-12-${(i % 30 + 1).toString().padStart(2, '0')}T${(8 + i).toString().padStart(2, '0')}:00:00"),
                    serviceId = serviceId
                )
            }
        }
        
        appointments.forEach { appointment ->
            client.post("/api/appointments") {
                contentType(ContentType.Application.Json)
                setBody(appointment)
            }
        }
        
        // Measure retrieval time for large dataset
        val retrievalTime = measureTime {
            val allAppointments = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
            assertTrue(allAppointments.success)
            assertEquals(numberOfServices * appointmentsPerService, allAppointments.data?.size)
        }
        
        println("Retrieved ${numberOfServices * appointmentsPerService} appointments in ${retrievalTime.inWholeMilliseconds}ms")
        
        // Measure service retrieval time
        val serviceRetrievalTime = measureTime {
            val allServices = client.get("/api/services").body<ApiResponse<List<Service>>>()
            assertTrue(allServices.success)
            assertEquals(numberOfServices, allServices.data?.size)
        }
        
        println("Retrieved $numberOfServices services in ${serviceRetrievalTime.inWholeMilliseconds}ms")
    }
    
    @Test
    fun testDoubleBookingPerformance() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // Create a service
        val serviceRequest = ServiceRequest(
            name = "Conflict Test Service",
            description = "Service for testing conflict detection performance",
            defaultDurationInMinutes = 60
        )
        
        val serviceResponse = client.post("/api/services") {
            contentType(ContentType.Application.Json)
            setBody(serviceRequest)
        }.body<ApiResponse<Service>>()
        
        val serviceId = serviceResponse.data?.id!!
        
        // Create a base appointment
        val baseAppointment = AppointmentRequest(
            clientName = "Base Client",
            clientEmail = "base@example.com",
            appointmentTime = LocalDateTime.parse("2024-12-01T10:00:00"),
            serviceId = serviceId
        )
        
        client.post("/api/appointments") {
            contentType(ContentType.Application.Json)
            setBody(baseAppointment)
        }
        
        // Create multiple valid appointments to increase dataset size
        val validAppointments = (1..50).map { i ->
            AppointmentRequest(
                clientName = "Valid Client $i",
                clientEmail = "valid$i@example.com",
                appointmentTime = LocalDateTime.parse("2024-12-${(i % 30 + 1).toString().padStart(2, '0')}T${(12 + i % 12).toString().padStart(2, '0')}:00:00"),
                serviceId = serviceId
            )
        }
        
        validAppointments.forEach { appointment ->
            client.post("/api/appointments") {
                contentType(ContentType.Application.Json)
                setBody(appointment)
            }
        }
        
        // Now test conflict detection performance with larger dataset
        val conflictingAppointments = (1..10).map { i ->
            AppointmentRequest(
                clientName = "Conflicting Client $i",
                clientEmail = "conflict$i@example.com",
                appointmentTime = LocalDateTime.parse("2024-12-01T10:30:00"), // Conflicts with base appointment
                serviceId = serviceId
            )
        }
        
        val conflictDetectionTime = measureTime {
            runBlocking {
                val results = conflictingAppointments.map { appointment ->
                    async {
                        client.post("/api/appointments") {
                            contentType(ContentType.Application.Json)
                            setBody(appointment)
                        }
                    }
                }.awaitAll()
                
                // Verify all appointments were rejected due to conflicts
                assertTrue(results.all { it.status == HttpStatusCode.Conflict })
            }
        }
        
        println("Detected ${conflictingAppointments.size} conflicts in ${conflictDetectionTime.inWholeMilliseconds}ms with ${validAppointments.size + 1} existing appointments")
    }
    
    @Test
    fun testUpdatePerformance() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // Create services
        val numberOfServices = 20
        val services = (1..numberOfServices).map { i ->
            ServiceRequest(
                name = "Service $i",
                description = "Update performance test service $i",
                defaultDurationInMinutes = 60
            )
        }
        
        val createdServices = services.map { service ->
            client.post("/api/services") {
                contentType(ContentType.Application.Json)
                setBody(service)
            }.body<ApiResponse<Service>>()
        }
        
        val serviceIds = createdServices.mapNotNull { it.data?.id }
        
        // Measure update performance
        val updateTime = measureTime {
            runBlocking {
                val updateResults = serviceIds.map { serviceId ->
                    async {
                        val updateRequest = ServiceRequest(
                            name = "Updated Service $serviceId",
                            description = "Updated description for service $serviceId",
                            defaultDurationInMinutes = 90
                        )
                        
                        client.put("/api/services/$serviceId") {
                            contentType(ContentType.Application.Json)
                            setBody(updateRequest)
                        }.body<ApiResponse<Service>>()
                    }
                }.awaitAll()
                
                // Verify all updates were successful
                assertTrue(updateResults.all { it.success })
            }
        }
        
        println("Updated $numberOfServices services in ${updateTime.inWholeMilliseconds}ms")
    }
    
    @Test
    fun testDeletePerformance() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        // Create services
        val numberOfServices = 30
        val services = (1..numberOfServices).map { i ->
            ServiceRequest(
                name = "Service $i",
                description = "Delete performance test service $i",
                defaultDurationInMinutes = 60
            )
        }
        
        val createdServices = services.map { service ->
            client.post("/api/services") {
                contentType(ContentType.Application.Json)
                setBody(service)
            }.body<ApiResponse<Service>>()
        }
        
        val serviceIds = createdServices.mapNotNull { it.data?.id }
        
        // Measure delete performance
        val deleteTime = measureTime {
            runBlocking {
                val deleteResults = serviceIds.map { serviceId ->
                    async {
                        client.delete("/api/services/$serviceId").body<ApiResponse<String>>()
                    }
                }.awaitAll()
                
                // Verify all deletions were successful
                assertTrue(deleteResults.all { it.success })
            }
        }
        
        println("Deleted $numberOfServices services in ${deleteTime.inWholeMilliseconds}ms")
        
        // Verify all services are deleted
        val remainingServices = client.get("/api/services").body<ApiResponse<List<Service>>>()
        assertTrue(remainingServices.success)
        assertTrue(remainingServices.data?.isEmpty() == true)
    }
    
    @Test
    fun testMemoryUsage() = testApplication {
        application {
            configureTestApplication()
        }
        
        val client = TestConfiguration.run { createJsonClient() }
        
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Create a large number of services and appointments
        val numberOfServices = 5
        val appointmentsPerService = 50
        
        // Create services
        val createdServices = (1..numberOfServices).map { i ->
            val service = ServiceRequest(
                name = "Memory Test Service $i",
                description = "Service for memory usage testing $i",
                defaultDurationInMinutes = 60
            )
            
            client.post("/api/services") {
                contentType(ContentType.Application.Json)
                setBody(service)
            }.body<ApiResponse<Service>>()
        }
        
        val serviceIds = createdServices.mapNotNull { it.data?.id }
        
        // Create appointments
        serviceIds.forEach { serviceId ->
            (1..appointmentsPerService).forEach { i ->
                val appointment = AppointmentRequest(
                    clientName = "Memory Test Client $serviceId-$i",
                    clientEmail = "memory$serviceId-$i@example.com",
                    appointmentTime = LocalDateTime.parse("2024-12-${(i % 30 + 1).toString().padStart(2, '0')}T${(8 + i % 14).toString().padStart(2, '0')}:00:00"),
                    serviceId = serviceId
                )
                
                client.post("/api/appointments") {
                    contentType(ContentType.Application.Json)
                    setBody(appointment)
                }
            }
        }
        
        // Force garbage collection
        runtime.gc()
        Thread.sleep(100)
        
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory
        
        println("Memory used for ${numberOfServices} services and ${numberOfServices * appointmentsPerService} appointments: ${memoryUsed / 1024 / 1024}MB")
        
        // Verify data integrity
        val allServices = client.get("/api/services").body<ApiResponse<List<Service>>>()
        val allAppointments = client.get("/api/appointments").body<ApiResponse<List<Appointment>>>()
        
        assertTrue(allServices.success)
        assertTrue(allAppointments.success)
        assertEquals(numberOfServices, allServices.data?.size)
        assertEquals(numberOfServices * appointmentsPerService, allAppointments.data?.size)
    }
}