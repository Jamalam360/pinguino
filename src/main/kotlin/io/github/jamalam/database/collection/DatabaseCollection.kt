package io.github.jamalam.database.collection

import com.mongodb.client.MongoCollection

open class DatabaseCollection<T>(val collection: MongoCollection<T>)
