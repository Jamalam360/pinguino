package io.github.jamalam.database

import com.mongodb.MongoSocketException

fun <T> tryOperationUntilSuccess(operation: () -> T): T {
    while (true) {
        try {
            return operation.invoke()
        } catch (_: MongoSocketException) {
        }
    }
}
