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

package io.github.jamalam.util

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.*

/**
 * @author  Jamalam360
 */
open class SingleChannelArgs : Arguments() {
    val channel by channel(
        "channel",
        "The channel"
    )
}

class SingleRoleArgs : Arguments() {
    val role by role(
        "role",
        "The role"
    )
}

class SingleBooleanArgs : Arguments() {
    val boolean by boolean(
        "enabled",
        "Whether to enable this option"
    )
}

open class SingleUserArgs : Arguments() {
    val user by user(
        "user",
        "The user"
    )
}

class SingleStringArgs : Arguments() {
    val string by string(
        "string",
        "The string"
    )
}
