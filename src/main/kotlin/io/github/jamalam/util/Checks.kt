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

import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event
import dev.kord.core.exception.EntityNotFoundException
import io.github.jamalam.Modules
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

/**
 * @author  Jamalam360
 */

suspend fun <T : Event> CheckContext<T>.isModuleEnabled(module: Modules) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("io.github.jamalam360.util.Checks.moduleEnabled")
    val guild = guildFor(event)

    if (guild == null) {
        logger.nullGuild(event)
        fail()
    } else {
        if (database.serverConfig.isModuleEnabled(guild.id, module)) {
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

    val logger = KotlinLogging.logger("io.github.jamalam360.util.Checks.hasModeratorRole")
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
                    .contains(guild.getRole(Snowflake(database.serverConfig.getConfig(guild.id).moderationConfig.moderatorRole)))
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

suspend fun <T : Event> CheckContext<T>.notExemptFromPhishing() {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("io.github.jamalam360.util.Checks.notExemptFromPhishing")
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
                    .contains(guild.getRole(Snowflake(database.serverConfig.getConfig(guild.id).moderationConfig.moderatorRole))) && guild.getConfig().phishingConfig.moderatorsExempt
            ) {
                logger.failed("Member ${member.id} has the moderator role set for this server")
                fail("Moderators are exempt from this check")
            } else {
                logger.passed()
                pass()
            }
        } catch (e: EntityNotFoundException) {
            logger.failed("Guild ${guild.id} does not have a role with the requested ID")
            fail("Moderators are exempt from this check")
        }
    }
}

suspend fun <T : Event> CheckContext<T>.ownsThread() {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("io.github.jamalam360.util.Checks.ownsThread")
    val guild = guildFor(event)
    val member = memberFor(event)
    val thread = threadFor(event)

    if (guild == null) {
        logger.nullGuild(event)
        fail()
    } else if (member == null) {
        logger.nullMember(event)
        fail()
    } else if (thread == null) {
        logger.nullChannel(event)
        fail()
    } else {
        if (thread.asChannel().ownerId != member.id) {
            logger.failed("Member ${member.id} does not own the thread ${thread.id}")
            fail("You must own the thread to execute this command")
        } else {
            logger.passed()
            pass()
        }
    }
}

suspend fun <T : Event> CheckContext<T>.notInDm() {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("io.github.jamalam360.util.Checks.notInDm")
    val guild = guildFor(event)

    if (guild == null) {
        logger.failed("Event not associated with a guild")
        fail("You must be in a guild to execute this command")
    } else {
        logger.passed()
        pass()
    }
}
