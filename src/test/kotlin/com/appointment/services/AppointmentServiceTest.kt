// src/test/kotlin/com/appointment/services/AppointmentServiceTest.kt
package com.appointment.services

import com.appointment.models.*
import com.appointment.repositories.AppointmentRepository
import com.appointment.repositories.ServiceRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppointmentServiceTest {

    private val mockAppointmentRepository = mockk<AppointmentRepository>()
    private val mockServiceRepository = mockk<ServiceRepository>()
    private val appointmentService = AppointmentServiceImpl(mockAppointmentRepository, mockServiceRepository)

    private val testService = Service(1, "Haircut", "Basic haircut", 30)
    private val testAppointment = Appointment(
        id = 1,
        clientName = "John Doe",
        clientEmail = "john@example.com",
        appointmentTime = LocalDateTime(2025, 8, 15, 10, 0),
        serviceId = 1
    )

    @Test
    fun `getAllAppointments should return all appointments with services`() = runTest {
        // Given
        val appointments = listOf(testAppointment)
        coEvery { mockAppointmentRepository.getAllAppointments() } returns appointments
        coEvery { mockServiceRepository.getServiceById(1) } returns testService

        // When
        val result = appointmentService.getAllAppointments()

        // Then
        assertEquals(1, result.size)
        assertEquals(testAppointment.clientName, result[0].clientName)
        assertEquals(testService.name, result[0].service.name)
        coVerify { mockAppointmentRepository.getAllAppointments() }
        coVerify { mockServiceRepository.getServiceById(1) }
    }

    @Test
    fun `getAppointmentById should return appointment when found`() = runTest {
        // Given
        coEvery { mockAppointmentRepository.getAppointmentById(1) } returns testAppointment
        coEvery { mockServiceRepository.getServiceById(1) } returns testService

        // When
        val result = appointmentService.getAppointmentById(1)

        // Then
        assertEquals(testAppointment.clientName, result.clientName)
        assertEquals(testService.name, result.service.name)
        coVerify { mockAppointmentRepository.getAppointmentById(1) }
        coVerify { mockServiceRepository.getServiceById(1) }
    }

    @Test
    fun `getAppointmentById should throw exception when not found`() = runTest {
        // Given
        coEvery { mockAppointmentRepository.getAppointmentById(1) } returns null

        // When & Then
        assertFailsWith<AppointmentNotFoundException> {
            appointmentService.getAppointmentById(1)
        }
    }

    @Test
    fun `createAppointment should create valid appointment`() = runTest {
        // Given
        val request = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime(2025, 8, 15, 10, 0),
            serviceId = 1
        )

        coEvery { mockServiceRepository.getServiceById(1) } returns testService
        coEvery { mockServiceRepository.serviceExists(1) } returns true
        coEvery { mockAppointmentRepository.getConflictingAppointments(any(), any(), any(), any()) } returns emptyList()
        coEvery { mockAppointmentRepository.createAppointment(request) } returns testAppointment

        // When
        val result = appointmentService.createAppointment(request)

        // Then
        assertEquals(testAppointment.clientName, result.clientName)
        assertEquals(testService.name, result.service.name)
        coVerify { mockAppointmentRepository.createAppointment(request) }
    }

    @Test
    fun `createAppointment should throw exception for invalid email`() = runTest {
        // Given
        val invalidRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "invalid-email",
            appointmentTime = LocalDateTime(2025, 8, 15, 10, 0),
            serviceId = 1
        )

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            appointmentService.createAppointment(invalidRequest)
        }
    }

    @Test
    fun `createAppointment should throw exception for past appointment time`() = runTest {
        // Given
        val invalidRequest = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime(2020, 1, 1, 10, 0),
            serviceId = 1
        )
        coEvery { mockServiceRepository.serviceExists(1) } returns true

        // When & Then
        assertFailsWith<InvalidDateTimeException> {
            appointmentService.createAppointment(invalidRequest)
        }
    }

    @Test
    fun `createAppointment should throw exception for double booking`() = runTest {
        // Given
        val request = AppointmentRequest(
            clientName = "John Doe",
            clientEmail = "john@example.com",
            appointmentTime = LocalDateTime(2025, 8, 15, 10, 0),
            serviceId = 1
        )

        val conflictingAppointment = Appointment(
            id = 2,
            clientName = "Jane Smith",
            clientEmail = "jane@example.com",
            appointmentTime = LocalDateTime(2025, 8, 15, 10, 0),
            serviceId = 1
        )

        coEvery { mockServiceRepository.getServiceById(1) } returns testService
        coEvery { mockServiceRepository.serviceExists(1) } returns true
        coEvery { mockAppointmentRepository.getConflictingAppointments(any(), any(), any(), any()) } returns listOf(conflictingAppointment)

        // When & Then
        assertFailsWith<DoubleBookingException> {
            appointmentService.createAppointment(request)
        }
    }

    @Test
    fun `updateAppointment should update existing appointment`() = runTest {
        // Given
        val request = AppointmentRequest(
            clientName = "John Updated",
            clientEmail = "john.updated@example.com",
            appointmentTime = LocalDateTime(2025, 8, 15, 11, 0),
            serviceId = 1
        )

        val updatedAppointment = testAppointment.copy(
            clientName = "John Updated",
            clientEmail = "john.updated@example.com",
            appointmentTime = LocalDateTime(2025, 8, 15, 11, 0)
        )

        coEvery { mockServiceRepository.getServiceById(1) } returns testService
        coEvery { mockServiceRepository.serviceExists(1) } returns true
        coEvery { mockAppointmentRepository.getConflictingAppointments(any(), any(), any(), any()) } returns emptyList()
        coEvery { mockAppointmentRepository.updateAppointment(1, request) } returns updatedAppointment

        // When
        val result = appointmentService.updateAppointment(1, request)

        // Then
        assertEquals(updatedAppointment.clientName, result.clientName)
        assertEquals(testService.name, result.service.name)
        coVerify { mockAppointmentRepository.updateAppointment(1, request) }
    }

    @Test
    fun `deleteAppointment should delete existing appointment`() = runTest {
        // Given
        coEvery { mockAppointmentRepository.deleteAppointment(1) } returns true

        // When
        val result = appointmentService.deleteAppointment(1)

        // Then
        assertTrue(result)
        coVerify { mockAppointmentRepository.deleteAppointment(1) }
    }
}