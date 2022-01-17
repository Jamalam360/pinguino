package io.github.jamalam360.util

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
