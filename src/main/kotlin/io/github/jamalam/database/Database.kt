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
}
