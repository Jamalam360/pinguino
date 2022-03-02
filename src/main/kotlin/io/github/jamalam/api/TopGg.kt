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

package io.github.jamalam.api

import io.github.jamalam.config.config
import io.github.jamalam.util.DBL_URL
import io.github.jamalam.util.client
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class TopGg {
    suspend fun sendServerCount(count: Int) {
        client.post<HttpResponse>(DBL_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", config.auth.dblToken!!)
            body = DBLStatisticBody(count)
        }
    }

    @Serializable
    data class DBLStatisticBody(
        @SerialName("server_count")
        val count: Int
    )
}
