// src/main/kotlin/com/appointment/Application.kt
package com.appointment

import com.appointment.database.DatabaseFactory
import com.appointment.models.HealthResponse
import com.appointment.repositories.AppointmentRepositoryImpl
import com.appointment.repositories.ServiceRepositoryImpl
import com.appointment.routes.appointmentRoutes
import com.appointment.routes.serviceRoutes
import com.appointment.services.AppointmentServiceImpl
import com.appointment.services.ServiceServiceImpl
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseFactory.init()

    // Initialize repositories
    val serviceRepository = ServiceRepositoryImpl()
    val appointmentRepository = AppointmentRepositoryImpl()

    // Initialize services
    val serviceService = ServiceServiceImpl(serviceRepository)
    val appointmentService = AppointmentServiceImpl(appointmentRepository, serviceRepository)

    configurePlugins()
    configureRouting(serviceService, appointmentService)
}

fun Application.configurePlugins() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf(
                    "error" to "INTERNAL_ERROR",
                    "message" to (cause.message ?: "Unknown error")
                )
            )
        }
    }
}

fun Application.configureRouting(
    serviceService: ServiceServiceImpl,
    appointmentService: AppointmentServiceImpl
) {
    routing {
        get("/") {
            call.respondText("Appointment Booking System API")
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "OK",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        serviceRoutes(serviceService)
        appointmentRoutes(appointmentService)
    }
}