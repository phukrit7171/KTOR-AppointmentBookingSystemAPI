// src/main/kotlin/com/appointment/database/Database.kt
package com.appointment.database

import com.appointment.models.AppointmentsTable
import com.appointment.models.ServicesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val database = Database.connect(
            url = "jdbc:h2:mem:appointment_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(ServicesTable, AppointmentsTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T {
        return org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction {
            block()
        }
    }
}