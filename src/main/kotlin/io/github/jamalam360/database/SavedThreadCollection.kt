package io.github.jamalam360.database

import com.mongodb.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import io.github.jamalam360.database.entity.SavedThread
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 * @author  Jamalam360
 */
@Suppress("RemoveExplicitTypeArguments")
class SavedThreadCollection(db: MongoDatabase) : DatabaseCollection<SavedThread>(db.getCollection<SavedThread>()) {
    fun setSave(thread: Snowflake, save: Boolean = true) {
        val has = collection.findOne(SavedThread::id eq thread.value.toLong()) != null

        if (has && !save) {
            collection.deleteOne(SavedThread::id eq thread.value.toLong())
        } else if (!has && save) {
            collection.insertOne(SavedThread(thread.value.toLong()))
        }
    }

    fun shouldSave(thread: Snowflake): Boolean = collection.findOne(SavedThread::id eq thread.value.toLong()) != null
}