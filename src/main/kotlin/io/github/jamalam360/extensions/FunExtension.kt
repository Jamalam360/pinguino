package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.common.annotation.KordPreview

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class FunExtension : Extension() {
    override val name: String = "fun"

    override suspend fun setup() {
        publicSlashCommand {
            name = "fun"
            description = "A selection of fun commands"
        }
    }
}
