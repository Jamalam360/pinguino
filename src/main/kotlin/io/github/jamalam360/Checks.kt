package io.github.jamalam360

import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event
import dev.kord.core.exception.EntityNotFoundException
import io.github.jamalam360.database.Modules
import kotlinx.coroutines.flow.toList
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

suspend fun <T : Event> CheckContext<T>.hasModeratorRole() {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("io.github.jamalam360.Checks.hasModeratorRole")
    val guild = guildFor(event)
    val member = memberFor(event)

    if (guild == null) {
        logger.nullGuild(event)
        fail()
    } else if (member == null) {
        logger.nullMember(event)
        fail()
    } else {
        try {
            if (member.asMember().roles.toList()
                    .contains(guild.getRole(Snowflake(DATABASE.config.getConfig(guild.id).moderationConfig.moderatorRole)))
            ) {
                logger.passed()
                pass()
            } else {
                logger.failed("Member ${member.id} does not have the moderator role set for this server")
                fail("The moderator role is required to execute this command")
            }
        } catch (e: EntityNotFoundException) {
            if (member.asMember().getPermissions().contains(Permission.Administrator)) {
                logger.passed("User does not have moderator role for server ${guild.id}, but they are an administrator")
                pass()
            } else {
                logger.failed("Guild ${guild.id} does not have a role with the requested ID")
                fail("The moderator role is required to execute this command, but the role could not be found - did you remember to set it?")
            }
        }
    }
}
