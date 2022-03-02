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

package io.github.jamalam.database.collection

import com.mongodb.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import io.github.jamalam.database.entity.SavedThread
import io.github.jamalam.database.tryOperationUntilSuccess
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 * @author  Jamalam360
 */
@Suppress("RemoveExplicitTypeArguments")
class SavedThreadCollection(db: MongoDatabase) :
    DatabaseCollection<Snowflake, SavedThread>(db.getCollection<SavedThread>()) {
    fun setSave(thread: Snowflake, save: Boolean = true) {
        val has: Boolean = if (cache.containsKey(thread)) {
            true
        } else {
            tryOperationUntilSuccess { collection.findOne(SavedThread::id eq thread.value.toLong()) != null }
        }

        if (has && !save) {
            tryOperationUntilSuccess { collection.deleteOne(SavedThread::id eq thread.value.toLong()) }

            if (cache.containsKey(thread)) {
                cache.remove(thread)
            }
        } else if (!has && save) {
            val savedThread = SavedThread(thread.value.toLong())
            tryOperationUntilSuccess { collection.insertOne(savedThread) }
            cache[thread] = savedThread
        }
    }

    fun shouldSave(thread: Snowflake): Boolean {
        if (cache.containsKey(thread)) return true
        return tryOperationUntilSuccess { collection.findOne(SavedThread::id eq thread.value.toLong()) != null }
    }
}