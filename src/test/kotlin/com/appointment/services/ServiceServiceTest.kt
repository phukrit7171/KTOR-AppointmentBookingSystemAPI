// src/test/kotlin/com/appointment/services/ServiceServiceTest.kt
package com.appointment.services

import com.appointment.models.*
import com.appointment.repositories.ServiceRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ServiceServiceTest {

    private val mockRepository = mockk<ServiceRepository>()
    private val serviceService = ServiceServiceImpl(mockRepository)

    @Test
    fun `getAllServices should return all services`() = runTest {
        // Given
        val services = listOf(
            Service(1, "Haircut", "Basic haircut", 30),
            Service(2, "Massage", "Relaxing massage", 60)
        )
        coEvery { mockRepository.getAllServices() } returns services

        // When
        val result = serviceService.getAllServices()

        // Then
        assertEquals(services, result)
        coVerify { mockRepository.getAllServices() }
    }

    @Test
    fun `getServiceById should return service when found`() = runTest {
        // Given
        val service = Service(1, "Haircut", "Basic haircut", 30)
        coEvery { mockRepository.getServiceById(1) } returns service

        // When
        val result = serviceService.getServiceById(1)

        // Then
        assertEquals(service, result)
        coVerify { mockRepository.getServiceById(1) }
    }

    @Test
    fun `getServiceById should throw exception when not found`() = runTest {
        // Given
        coEvery { mockRepository.getServiceById(1) } returns null

        // When & Then
        assertFailsWith<ServiceNotFoundException> {
            serviceService.getServiceById(1)
        }
    }

    @Test
    fun `createService should create valid service`() = runTest {
        // Given
        val request = ServiceRequest("Haircut", "Basic haircut", 30)
        val service = Service(1, "Haircut", "Basic haircut", 30)
        coEvery { mockRepository.createService(request) } returns service

        // When
        val result = serviceService.createService(request)

        // Then
        assertEquals(service, result)
        coVerify { mockRepository.createService(request) }
    }

    @Test
    fun `createService should throw exception for invalid data`() = runTest {
        // Given
        val invalidRequest = ServiceRequest("", "Description", 30)

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            serviceService.createService(invalidRequest)
        }
    }

    @Test
    fun `createService should throw exception for invalid duration`() = runTest {
        // Given
        val invalidRequest = ServiceRequest("Service", "Description", 0)

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            serviceService.createService(invalidRequest)
        }
    }

    @Test
    fun `updateService should update existing service`() = runTest {
        // Given
        val request = ServiceRequest("Updated Haircut", "Updated description", 45)
        val service = Service(1, "Updated Haircut", "Updated description", 45)
        coEvery { mockRepository.updateService(1, request) } returns service

        // When
        val result = serviceService.updateService(1, request)

        // Then
        assertEquals(service, result)
        coVerify { mockRepository.updateService(1, request) }
    }

    @Test
    fun `deleteService should delete existing service`() = runTest {
        // Given
        coEvery { mockRepository.deleteService(1) } returns true

        // When
        val result = serviceService.deleteService(1)

        // Then
        assertTrue(result)
        coVerify { mockRepository.deleteService(1) }
    }
}

