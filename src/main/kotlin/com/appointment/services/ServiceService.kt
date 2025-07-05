package com.appointment.services

import com.appointment.models.Service
import com.appointment.models.ServiceRequest
import com.appointment.models.ServiceNotFoundException
import com.appointment.repositories.ServiceRepository

class ServiceServiceImpl(private val repository: ServiceRepository) {
    
    suspend fun getAllServices(): List<Service> {
        return repository.getAllServices()
    }
    
    suspend fun getServiceById(id: Int): Service {
        return repository.getServiceById(id) 
            ?: throw ServiceNotFoundException("Service with ID $id not found")
    }
    
    suspend fun createService(request: ServiceRequest): Service {
        // Validate request
        validateServiceRequest(request)
        
        // Create service
        return repository.createService(request)
    }
    
    suspend fun updateService(id: Int, request: ServiceRequest): Service {
        // Validate request
        validateServiceRequest(request)
        
        // Update service
        return repository.updateService(id, request)
            ?: throw ServiceNotFoundException("Service with ID $id not found")
    }
    
    suspend fun deleteService(id: Int): Boolean {
        val deleted = repository.deleteService(id)
        if (!deleted) {
            throw ServiceNotFoundException("Service with ID $id not found")
        }
        return true
    }
    
    private fun validateServiceRequest(request: ServiceRequest) {
        // Name and description cannot be blank
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Service name cannot be blank")
        }
        
        if (request.description.isBlank()) {
            throw IllegalArgumentException("Service description cannot be blank")
        }
        
        // Duration must be positive and not exceed 24 hours (1440 minutes)
        if (request.defaultDurationInMinutes <= 0) {
            throw IllegalArgumentException("Service duration must be positive")
        }
        
        if (request.defaultDurationInMinutes > 1440) {
            throw IllegalArgumentException("Service duration cannot exceed 24 hours (1440 minutes)")
        }
    }
}