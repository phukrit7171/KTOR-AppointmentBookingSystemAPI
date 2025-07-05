package com.appointment.routes

import com.appointment.models.*
import com.appointment.services.ServiceServiceImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.serviceRoutes(serviceService: ServiceServiceImpl) {
    route("/api/services") {

        get {
            try {
                val services = serviceService.getAllServices()
                call.respond(HttpStatusCode.OK, services)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error")
                )
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid service ID")
                    )

                val service = serviceService.getServiceById(id)
                call.respond(HttpStatusCode.OK, service)
            } catch (e: ServiceNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("SERVICE_NOT_FOUND", e.message ?: "Service not found")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error")
                )
            }
        }

        post {
            try {
                val serviceRequest = call.receive<ServiceRequest>()
                val service = serviceService.createService(serviceRequest)
                call.respond(HttpStatusCode.Created, service)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", e.message ?: "Invalid service data")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error")
                )
            }
        }

        put("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid service ID")
                    )

                val serviceRequest = call.receive<ServiceRequest>()
                val service = serviceService.updateService(id, serviceRequest)
                call.respond(HttpStatusCode.OK, service)
            } catch (e: ServiceNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("SERVICE_NOT_FOUND", e.message ?: "Service not found")
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", e.message ?: "Invalid service data")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error")
                )
            }
        }

        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid service ID")
                    )

                serviceService.deleteService(id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: ServiceNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("SERVICE_NOT_FOUND", e.message ?: "Service not found")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Unknown error")
                )
            }
        }
    }
}