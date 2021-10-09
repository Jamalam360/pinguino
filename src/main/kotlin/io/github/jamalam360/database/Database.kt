package io.github.jamalam360.database

import io.github.jamalam360.PRODUCTION
import org.litote.kmongo.KMongo

/**
 * @author  Jamalam360
 */

class Database {
    private val client = KMongo.createClient()
    private val db = if (PRODUCTION) {
        client.getDatabase("PinguinoProductionDatabase")
    } else {
        client.getDatabase("PinguinoTestingDatabase")
    }

    val config = ConfigCollection(db)
}
