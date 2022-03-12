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

package io.github.jamalam.pinguino.database

import com.mongodb.MongoSocketException
import io.github.jamalam.pinguino.util.logger
import java.net.SocketTimeoutException

@Suppress("TooGenericExceptionCaught")
fun <T> tryOperationUntilSuccess(operation: () -> T): T {
    var i = 1
    while (true) {
        try {
            return operation.invoke()
        } catch (e: Exception) {
            when (e) {
                is MongoSocketException, is SocketTimeoutException -> logger.warn {
                    "Caught socket exception ($i) when performing database operation; retrying"
                }
                else -> throw e
            }
        }
        i++
    }
}
