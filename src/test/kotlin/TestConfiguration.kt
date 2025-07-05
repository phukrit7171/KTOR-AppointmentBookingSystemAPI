package com.camt

import com.camt.database.Appointments
import com.camt.database.Services
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.camt.plugins.*

/**
 * Test configuration utilities and shared setup
 */
object TestConfiguration {
    
    /**
     * Sets up an in-memory H2 database for testing
     */
    fun setupTestDatabase() {
        Database.connect(
            url = "jdbc:h2:mem:testdb${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver"
        )
        
        transaction {
            SchemaUtils.create(Services, Appointments)
        }
    }
    
    /**
     * Cleans up the test database
     */
    fun cleanupTestDatabase() {
        transaction {
            SchemaUtils.drop(Services, Appointments)
        }
    }
    
    /**
     * Creates a test client with JSON content negotiation configured
     */
    fun ApplicationTestBuilder.createJsonClient(): HttpClient {
        return createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }
    }
    
    /**
     * Configures the test application with all necessary plugins
     */

}
fun Application.configureTestApplication() {
    configureDatabase()
    configureSerialization()
    configureRouting()
    configureStatusPages()
    configureCORS()
    configureCallLogging()
}