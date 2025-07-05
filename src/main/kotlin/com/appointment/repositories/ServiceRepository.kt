// src/main/kotlin/com/appointment/repositories/ServiceRepository.kt
package com.appointment.repositories

import com.appointment.database.DatabaseFactory.dbQuery
import com.appointment.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ServiceRepository {
    suspend fun getAllServices(): List<Service>
    suspend fun getServiceById(id: Int): Service?
    suspend fun createService(service: ServiceRequest): Service
    suspend fun updateService(id: Int, service: ServiceRequest): Service?
    suspend fun deleteService(id: Int): Boolean
    suspend fun serviceExists(id: Int): Boolean
}

class ServiceRepositoryImpl : ServiceRepository {
    override suspend fun getAllServices(): List<Service> = dbQuery {
        ServiceEntity.all().map { it.toModel() }
    }

    override suspend fun getServiceById(id: Int): Service? = dbQuery {
        ServiceEntity.findById(id)?.toModel()
    }

    override suspend fun createService(service: ServiceRequest): Service = dbQuery {
        ServiceEntity.new {
            name = service.name
            description = service.description
            defaultDurationInMinutes = service.defaultDurationInMinutes
        }.toModel()
    }

    override suspend fun updateService(id: Int, service: ServiceRequest): Service? = dbQuery {
        ServiceEntity.findById(id)?.apply {
            name = service.name
            description = service.description
            defaultDurationInMinutes = service.defaultDurationInMinutes
        }?.toModel()
    }

    override suspend fun deleteService(id: Int): Boolean = dbQuery {
        ServiceEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    override suspend fun serviceExists(id: Int): Boolean = dbQuery {
        ServiceEntity.findById(id) != null
    }
}

