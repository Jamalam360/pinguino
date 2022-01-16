package io.github.jamalam360.database

import com.mongodb.client.MongoDatabase
import io.github.jamalam360.util.MONGO_SRV_URL
import io.github.jamalam360.util.PRODUCTION
import org.litote.kmongo.KMongo

/**
 * @author  Jamalam360
 */

class Database {
    private val client = if (!PRODUCTION) {
        KMongo.createClient(MONGO_SRV_URL)
    } else {
        KMongo.createClient()
    }

    val db: MongoDatabase = if (!PRODUCTION) {
        client.getDatabase("pinguino_production_db")
    } else {
        client.getDatabase("pinguino_testing_db")
    }

    val config = ConfigCollection(db)
    val savedThreads = SavedThreadCollection(db)
}
