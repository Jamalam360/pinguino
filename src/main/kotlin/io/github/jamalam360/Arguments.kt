package io.github.jamalam360

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.commands.converters.impl.user

/**
 * @author  Jamalam360
 */
class SingleChannelArgs : Arguments() {
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
