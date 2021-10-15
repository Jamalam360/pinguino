package io.github.jamalam360

import com.kotlindiscord.kord.extensions.checks.failed
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.nullGuild
import com.kotlindiscord.kord.extensions.checks.passed
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.core.event.Event
import io.github.jamalam360.database.Modules
import mu.KotlinLogging

/**
 * @author  Jamalam360
 */

suspend fun <T : Event> CheckContext<T>.moduleEnabled(module: Modules) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("io.github.jamalam360.Checks.moduleEnabled")
    val guild = guildFor(event)

    if (guild == null) {
        logger.nullGuild(event)
        fail()
    } else {
        if (DATABASE.config.isModuleEnabled(guild.id, module)) {
            logger.passed()
            pass()
        } else {
            logger.failed("Guild ${guild.id} does not have module '${module.readableName}' enabled")
            fail("This guild does not have the ${module.readableName} module enabled")
        }
    }
}
