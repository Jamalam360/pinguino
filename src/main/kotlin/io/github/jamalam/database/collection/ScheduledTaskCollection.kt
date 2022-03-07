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
import io.github.jamalam.database.entity.ScheduledTask
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection

@Suppress("RemoveExplicitTypeArguments")
class ScheduledTaskCollection(db: MongoDatabase) :
    DatabaseCollection<ScheduledTask>(db.getCollection<ScheduledTask>()) {

    private val cache: MutableList<ScheduledTask> = mutableListOf()

    fun addTask(task: ScheduledTask) {
        if (!task.type.validator.invoke(task.data)) {
            throw UnsupportedOperationException("Incorrect data for type ${task.type}: \n\r ${task.data}")
        }

        cache.add(task)
        collection.insertOne(task)
    }

    fun getTasks(): List<ScheduledTask> {
        return if (cache.isEmpty()) {
            val list = mutableListOf<ScheduledTask>()
            collection.find().forEach {
                list.add(it)
                cache.add(it)
            }
            list
        } else {
            cache
        }
    }

    fun removeTask(task: ScheduledTask) {
        cache.remove(task)

        collection.deleteOne(
            and(
                ScheduledTask::startTime eq task.startTime,
                ScheduledTask::type eq task.type,
                ScheduledTask::data eq task.data,
                ScheduledTask::duration eq task.duration,
            )
        )
    }

    fun removeTaskByData(data: Map<String, String>) {
        cache.remove(cache.filter {
            it.data == data
        }[0])

        collection.deleteOne(
            ScheduledTask::data eq data
        )
    }
}
