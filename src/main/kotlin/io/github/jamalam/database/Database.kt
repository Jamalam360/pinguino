/*
 * Copyright (C) 2022 Jamalam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.jamalam.database

import com.mongodb.client.MongoDatabase
import io.github.jamalam.config.config
import io.github.jamalam.database.entity.SavedThread
import io.github.jamalam.database.entity.ServerConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */


@OptIn(ExperimentalTime::class)
class Database {
    private val client = KMongo.createClient("${config.auth.mongoSrvUrl}?retryWrites=false&w=majority")
    private val logger = KotlinLogging.logger { }

    val db: MongoDatabase = if (config.production()) {
        client.getDatabase("pinguino_production_db")
    } else {
        client.getDatabase("pinguino_testing_db")
    }

    val serverConfig = ConfigCollection(db)
    val savedThreads = SavedThreadCollection(db)

    fun backup() {
        if (config.production() && config.production!!.databaseBackup == true) {
            logger.info("Starting database backup")

            val date = Date()
            val dateFormat = SimpleDateFormat("HH-mm-ss-SSSS_dd-MM-yyyy")
            val directory = config.production.databaseBackupDirectory!! + "/" + dateFormat.format(date)
            File(directory).mkdirs()


            logger.info("server_config has ${serverConfig.collection.countDocuments()} documents to backup")
            val documentsConfig = mutableListOf<ServerConfig>()
            serverConfig.collection.find().forEach { document ->
                documentsConfig.add(document)
            }

            val f1 = File("$directory/server_config.json")
            f1.createNewFile()
            f1.writeText(Json.encodeToString(ServerConfigCollectionBackup(documentsConfig)))

            logger.info("saved_thread has ${serverConfig.collection.countDocuments()} documents to backup")
            val documentsThread = mutableListOf<SavedThread>()
            savedThreads.collection.find().forEach { document ->
                documentsThread.add(document)
            }

            val f2 = File("$directory/saved_thread.json")
            f2.createNewFile()
            f2.writeText(Json.encodeToString(SavedThreadCollectionBackup(documentsThread)))

            logger.info("Finished backup at ${dateFormat.format(date)}")
        }
    }

    @Serializable
    data class ServerConfigCollectionBackup(
        val documents: List<ServerConfig>
    )

    @Serializable
    data class SavedThreadCollectionBackup(
        val documents: List<SavedThread>
    )
}
