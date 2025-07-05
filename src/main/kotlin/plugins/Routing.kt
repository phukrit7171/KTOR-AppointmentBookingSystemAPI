package com.camt.plugins


import com.camt.database.DatabaseManager
import com.camt.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val databaseManager = DatabaseManager()

    routing {

        get("/") {
            call.respondText("Hello Mr.Phukrit Kittinontana")
        }

        route("/api") {
            // Services routes
            route("/services") {
                get {
                    val services = databaseManager.getAllServices()
                    call.respond(
                        ApiResponse(
                            success = true,
                            message = "Services retrieved successfully",
                            data = services
                        )
                    )
                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Service>(
                                success = false,
                                message = "Invalid service ID"
                            )
                        )
                        return@get
                    }

                    val service = databaseManager.getServiceById(id)
                    if (service == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse<Service>(
                                success = false,
                                message = "Service not found"
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponse(
                                success = true,
                                message = "Service retrieved successfully",
                                data = service
                            )
                        )
                    }
                }

                post {
                    val request = call.receive<ServiceRequest>()
                    val service = databaseManager.createService(request)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            message = "Service created successfully",
                            data = service
                        )
                    )
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Service>(
                                success = false,
                                message = "Invalid service ID"
                            )
                        )
                        return@put
                    }

                    val request = call.receive<ServiceRequest>()
                    val service = databaseManager.updateService(id, request)
                    if (service == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse<Service>(
                                success = false,
                                message = "Service not found"
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponse(
                                success = true,
                                message = "Service updated successfully",
                                data = service
                            )
                        )
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<String>(
                                success = false,
                                message = "Invalid service ID"
                            )
                        )
                        return@delete
                    }

                    val deleted = databaseManager.deleteService(id)
                    if (deleted) {
                        call.respond(
                            ApiResponse(
                                success = true,
                                message = "Service deleted successfully",
                                data = "Service deleted"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<String>(
                                success = false,
                                message = "Cannot delete service with existing appointments"
                            )
                        )
                    }
                }
            }

            // Appointments routes
            route("/appointments") {
                get {
                    val appointments = databaseManager.getAllAppointments()
                    call.respond(
                        ApiResponse(
                            success = true,
                            message = "Appointments retrieved successfully",
                            data = appointments
                        )
                    )
                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Appointment>(
                                success = false,
                                message = "Invalid appointment ID"
                            )
                        )
                        return@get
                    }

                    val appointment = databaseManager.getAppointmentById(id)
                    if (appointment == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse<Appointment>(
                                success = false,
                                message = "Appointment not found"
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponse(
                                success = true,
                                message = "Appointment retrieved successfully",
                                data = appointment
                            )
                        )
                    }
                }

                post {
                    val request = call.receive<AppointmentRequest>()
                    val appointment = databaseManager.createAppointment(request)

                    if (appointment == null) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            ApiResponse<Appointment>(
                                success = false,
                                message = "Cannot create appointment: Service not found or time slot already booked"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Created,
                            ApiResponse(
                                success = true,
                                message = "Appointment created successfully",
                                data = appointment
                            )
                        )
                    }
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Appointment>(
                                success = false,
                                message = "Invalid appointment ID"
                            )
                        )
                        return@put
                    }

                    val request = call.receive<AppointmentRequest>()
                    val appointment = databaseManager.updateAppointment(id, request)

                    if (appointment == null) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            ApiResponse<Appointment>(
                                success = false,
                                message = "Cannot update appointment: Service not found, appointment not found, or time slot already booked"
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponse(
                                success = true,
                                message = "Appointment updated successfully",
                                data = appointment
                            )
                        )
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<String>(
                                success = false,
                                message = "Invalid appointment ID"
                            )
                        )
                        return@delete
                    }

                    val deleted = databaseManager.deleteAppointment(id)
                    if (deleted) {
                        call.respond(
                            ApiResponse(
                                success = true,
                                message = "Appointment deleted successfully",
                                data = "Appointment deleted"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse<String>(
                                success = false,
                                message = "Appointment not found"
                            )
                        )
                    }
                }
            }
        }
    }
}