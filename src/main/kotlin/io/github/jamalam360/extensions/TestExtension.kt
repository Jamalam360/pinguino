package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import io.github.jamalam360.PRODUCTION
import io.github.jamalam360.TEST_SERVER_ID

@OptIn(KordPreview::class)
class TestExtension : Extension() {
    override val name = "test"

    override suspend fun setup() {
        publicSlashCommand(::SlapSlashArgs) {
            name = "slap"
            description = "Ask the bot to slap another user"

            if (!PRODUCTION) {
                guild(TEST_SERVER_ID)
            }

            action {
                // Because of the DslMarker annotation KordEx uses, we need to grab Kord explicitly
                val kord = this@TestExtension.kord

                // Don't slap ourselves on request, slap the requester!
                val realTarget = if (arguments.target.id == kord.selfId) {
                    member
                } else {
                    arguments.target
                }

                respond {
                    content = "*slaps ${realTarget?.mention} with their ${arguments.weapon}*"
                }
            }
        }
    }

    inner class SlapSlashArgs : Arguments() {
        val target by user("target", description = "Person you want to slap")

        // Coalesced strings are not currently supported by slash commands
        val weapon by defaultingString(
            "weapon",

            defaultValue = "large, smelly trout",
            description = "What you want to slap with"
        )
    }
}
