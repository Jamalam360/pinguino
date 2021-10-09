package io.github.jamalam360.database

import com.mongodb.client.MongoCollection

/**
 * @author  Jamalam360
 */

open class DatabaseCollection<T>(val collection: MongoCollection<T>)
