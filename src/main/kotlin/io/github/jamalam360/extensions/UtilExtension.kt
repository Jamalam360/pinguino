package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.message.EmbedBuilder

/**
 * Random commands that don't fit elsewhere.
 * @author  Jamalam360
 */

@SuppressWarnings("MaxLineLength")
@OptIn(KordPreview::class)
class UtilExtension : Extension() {
    override val name: String = "util"

    override suspend fun setup() {
        publicSlashCommand {
            name = "invite"
            description = "Get an invite link for Pinguino!"

            action {
                val embed = EmbedBuilder()
                // TODO: Update invite link when bot goes public
                embed.image =
                    "https://images-ext-2.discordapp.net/external/tM2ezTNgh6TK_9IW5eCGQLtuaarLJfjdRgJ3hmRQ5rs" +
                            "/%3Fsize%3D256/https/cdn.discordapp.com/avatars/896758540784500797/507601ac" +
                            "31f51ffc334fac125089f7ea.png"
                embed.title = "Invite Pinguino!"
                embed.description = "Click [here](https://google.com/) to invite Pinguino to your own server"

                respond {
                    embeds.add(embed)
                }
            }
        }
    }
}
