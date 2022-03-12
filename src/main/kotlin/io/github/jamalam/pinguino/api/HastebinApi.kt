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

import io.github.jamalam.pinguino.util.client
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.Serializable

/**
 * @author  Jamalam360
 */
class HastebinApi {
    suspend fun pasteFromCdn(url: String, cdnUrl: String): String {
        return paste(
            url, String(
                client.get<HttpResponse>(cdnUrl).content.toInputStream()
                .readAllBytes())
        )
    }

    suspend fun paste(url: String, text: String): String {
        client.post<HastebinApiResponse>(url + "documents") {
            body = text
        }.let {
            return it.key
        }
    }

    @Serializable
    data class HastebinApiResponse(val key: String)
}
