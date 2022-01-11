package io.github.jamalam360.database

import com.mongodb.client.MongoDatabase
import io.github.jamalam360.VERSION
import io.github.jamalam360.database.entity.BotMeta
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne

/**
 * @author  Jamalam360
 */
@Suppress("RemoveExplicitTypeArguments")
class BotMetaCollection(db: MongoDatabase) : DatabaseCollection<BotMeta>(db.getCollection<BotMeta>()) {
    private val baseKey = "botMeta"

    fun get(): BotMeta {
        if (collection.countDocuments() == 0L) {
            collection.insertOne(
                BotMeta(
                    baseKey,
                    VERSION
                )
            )
        }

        return collection.findOne()!!
    }

    fun set(updated: BotMeta) {
        collection.updateOne(BotMeta::key eq baseKey, updated)
    }
}