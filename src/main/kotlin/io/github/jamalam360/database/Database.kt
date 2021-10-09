package io.github.jamalam360.database

import io.github.jamalam360.PRODUCTION
import org.litote.kmongo.KMongo

/**
 * @author  Jamalam360
 */

class Database {
    private val client = KMongo.createClient()
    private val db = if (PRODUCTION) {
        client.getDatabase("ProductionDatabase")
    } else {
        client.getDatabase("TestingDatabase")
    }

    val config = ConfigCollection(db)
}
