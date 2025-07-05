package com.appointment.routes

import com.appointment.models.*
import com.appointment.services.AppointmentServiceImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.appointmentRoutes(appointmentService: AppointmentServiceImpl) {
    route("/api/appointments") {

        get {
            try {
                val appointments = appointmentService.getAllAppointments()
                call.respond(HttpStatusCode.OK, appointments)
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
                        ErrorResponse("INVALID_ID", "Invalid appointment ID")
                    )

                val appointment = appointmentService.getAppointmentById(id)
                call.respond(HttpStatusCode.OK, appointment)
            } catch (e: AppointmentNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("APPOINTMENT_NOT_FOUND", e.message ?: "Appointment not found")
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
                val appointmentRequest = call.receive<AppointmentRequest>()
                val appointment = appointmentService.createAppointment(appointmentRequest)
                call.respond(HttpStatusCode.Created, appointment)
            } catch (e: ServiceNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("SERVICE_NOT_FOUND", e.message ?: "Service not found")
                )
            } catch (e: DoubleBookingException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("DOUBLE_BOOKING", e.message ?: "Time slot already booked")
                )
            } catch (e: InvalidDateTimeException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("INVALID_DATETIME", e.message ?: "Invalid appointment time")
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", e.message ?: "Invalid appointment data")
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
                        ErrorResponse("INVALID_ID", "Invalid appointment ID")
                    )

                val appointmentRequest = call.receive<AppointmentRequest>()
                val appointment = appointmentService.updateAppointment(id, appointmentRequest)
                call.respond(HttpStatusCode.OK, appointment)
            } catch (e: AppointmentNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("APPOINTMENT_NOT_FOUND", e.message ?: "Appointment not found")
                )
            } catch (e: ServiceNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("SERVICE_NOT_FOUND", e.message ?: "Service not found")
                )
            } catch (e: DoubleBookingException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("DOUBLE_BOOKING", e.message ?: "Time slot already booked")
                )
            } catch (e: InvalidDateTimeException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("INVALID_DATETIME", e.message ?: "Invalid appointment time")
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", e.message ?: "Invalid appointment data")
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
                        ErrorResponse("INVALID_ID", "Invalid appointment ID")
                    )

                appointmentService.deleteAppointment(id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: AppointmentNotFoundException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("APPOINTMENT_NOT_FOUND", e.message ?: "Appointment not found")
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
