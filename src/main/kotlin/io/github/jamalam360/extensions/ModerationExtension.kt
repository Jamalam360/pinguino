package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission

/**
 * @author Jamalam360
 */

@OptIn(KordPreview::class)
class ModerationExtension : Extension() {
    override val name: String = "fun"

    override suspend fun setup() {
        publicSlashCommand {
            name = "moderation"
            description = "Commands to help moderators moderate the server"

            check {
                hasPermission(Permission.Administrator)
            }
        }
    }
}
