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
