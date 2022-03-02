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
import io.github.jamalam.database.collection.AnnouncementSubscriberCollection
import io.github.jamalam.database.collection.ConfigCollection
import io.github.jamalam.database.collection.SavedThreadCollection
import org.litote.kmongo.KMongo
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */

@OptIn(ExperimentalTime::class)
class Database {
    private val client = KMongo.createClient(
        if (config.auth.mongoSrvUrl.contains("localhost")) {
            config.auth.mongoSrvUrl
        } else "${config.auth.mongoSrvUrl}?retryWrites=false&w=majority"
    )

    val db: MongoDatabase = if (config.production()) {
        client.getDatabase("pinguino_production_db")
    } else {
        client.getDatabase("pinguino_testing_db")
    }

    val serverConfig = ConfigCollection(db)
    val savedThreads = SavedThreadCollection(db)
    val announcementSubscribers = AnnouncementSubscriberCollection(db)
}
