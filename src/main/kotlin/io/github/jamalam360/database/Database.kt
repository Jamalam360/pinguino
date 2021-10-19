package io.github.jamalam360.database

import com.mongodb.client.MongoDatabase
import io.github.jamalam360.PRODUCTION
import org.litote.kmongo.KMongo

/**
 * @author  Jamalam360
 */

class Database {
    private val client = KMongo.createClient()
    val db: MongoDatabase = if (PRODUCTION) {
        client.getDatabase("PinguinoProductionDatabase")
    } else {
        client.getDatabase("PinguinoTestingDatabase")
    }

    val config = ConfigCollection(db)
}
