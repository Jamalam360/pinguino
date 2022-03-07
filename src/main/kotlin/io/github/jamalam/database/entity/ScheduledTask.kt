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

package io.github.jamalam.database.entity

enum class ScheduledTaskType(val validator: (data: Map<String, String>) -> Boolean) {
    PostMessageToChannel({ data ->
        data.containsKey("channel") && data.containsKey("message")
    }),
    PostEmbedToChannel({ data ->
        data.containsKey("title") && data.containsKey("description") && data.containsKey("image") &&
                data.containsKey("author") && data.containsKey("channel")
    }),
    PostUnmutedLogs({ data ->
        data.containsKey("guild") && data.containsKey("member") && data.containsKey("moderator")
    }),
    PostChannelUnlockedLogs({ data ->
        data.contains("channel") && data.containsKey("moderator") && data.containsKey("type")
    })
}

data class ScheduledTask(
    val startTime: Long,
    val duration: Long,
    val type: ScheduledTaskType,
    val data: Map<String, String>
)
