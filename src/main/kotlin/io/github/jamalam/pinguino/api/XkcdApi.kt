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

package io.github.jamalam.pinguino.api

import io.github.jamalam.pinguino.util.lenientClient
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class XkcdApi {
    suspend fun getComic(n: Int): XkcdApiResponse {
        return lenientClient.get(
                "https://xkcd.com/$n/info.0.json"
        )
    }

    suspend fun getLatestComic(): XkcdApiResponse {
        return lenientClient.get(
                "https://xkcd.com/info.0.json"
        )
    }

    @Serializable
    data class XkcdApiResponse(
        val month: String,
        val num: Int,
        val link: String,
        val year: String,
        val news: String,
        @SerialName("safe_title")
        val safeTitle: String,
        val transcript: String,
        val alt: String,
        val img: String,
        val title: String,
        val day: String
    )
}
