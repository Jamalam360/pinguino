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

package io.github.jamalam360.api

import io.github.jamalam360.util.lenientClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class DadJokeApi {
    suspend fun getTheFunnyHaHa(): String {
        return lenientClient.get<DadJokeApiResponse>("https://icanhazdadjoke.com/") {
            contentType(ContentType.Application.Json)
        }.joke
    }

    @Serializable
    data class DadJokeApiResponse(val id: String, val joke: String, val status: Int)
}