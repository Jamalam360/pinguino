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

package io.github.jamalam.pinguino.extensions.moderation

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.enum
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.getTopRole
import com.kotlindiscord.kord.extensions.utils.selfMember
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.pinguino.Modules
import io.github.jamalam.pinguino.database.entity.ServerConfig
import io.github.jamalam.pinguino.database.entity.ServerPhishingModerationType
import io.github.jamalam.pinguino.util.*

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class ModuleExtension : Extension() {
    override val name: String = "modules"

    private val roleModule: String = "the Roles module"
    private val tagsModule: String = "the Tags module"
    private val quotesModule: String = "the Quotes module"
    private val loggingModule: String = "the Logging module"
    private val moderationModule: String = "the Moderation module"
    private val notificationsModule: String = "the Greetings module"
    private val filePasteModule: String = "the File Paste module"
    private val phishingModule: String = "the Phishing module"

    private suspend fun SlashGroup.moduleEnable(moduleName: String, lambda: (ServerConfig) -> Unit) {
        ephemeralSubCommand {
            name = "enable"
            description = "Enables the $moduleName module"

            action {
                val config = guild!!.getConfig()
                lambda(config)
                database.serverConfig.updateConfig(guild!!.id, config)

                logModuleEnabled(moduleName, user, guild!!)

                respond {
                    embed {
                        info("Enabled $moduleName module")
                        pinguino()
                        now()
                        success()
                    }
                }
            }
        }
    }

    private suspend fun SlashGroup.moduleDisable(moduleName: String, lambda: (ServerConfig) -> Unit) {
        ephemeralSubCommand {
            name = "disable"
            description = "Disables the $moduleName module"

            action {
                val config = guild!!.getConfig()
                lambda(config)
                database.serverConfig.updateConfig(guild!!.id, config)

                logModuleDisabled(moduleName, user, guild!!)

                respond {
                    embed {
                        info("Disabled $moduleName module")
                        pinguino()
                        now()
                        success()
                    }
                }
            }
        }
    }

    @Suppress("DuplicatedCode")
    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "module"
            description = "Alter the settings of a specific module"

            check {
                notInDm()
                hasModeratorRole()
            }

            group("role") {
                description = "Alter the settings of $roleModule"

                moduleEnable("Roles") { conf ->
                    conf.roleConfig.enabled = true
                }

                moduleDisable("Roles") { conf ->
                    conf.roleConfig.enabled = false
                }

                ephemeralSubCommand(ModuleExtension::AddRoleArgs) {
                    name = "add-role"
                    description = "Add a role to the list of roles that a user can apply to themselves using `/role`"

                    action {
                        val name = arguments.name ?: arguments.role.name
                        val conf = guild!!.getConfig()

                        if (conf.roleConfig.roles.containsKey(name)) {
                            respond {
                                embed {
                                    info("A role is already added with that name")
                                    error()
                                    pinguino()
                                    now()
                                }
                            }
                        } else {
                            if (guild!!.selfMember().getTopRole()?.rawPosition != null && arguments.role.rawPosition >=
                                guild!!.selfMember().getTopRole()?.rawPosition!!
                            ) {
                                respond {
                                    embed {
                                        info("That role is higher than my top role, so I can't add it to users")
                                        error()
                                        pinguino()
                                        now()
                                    }
                                }
                            } else {
                                conf.roleConfig.roles[name] = arguments.role.id.value.toLong()
                                database.serverConfig.updateConfig(guild!!.id, conf)

                                guild!!.getLogChannel()?.createEmbed {
                                    info("Role added to `/role` roles")
                                    userAuthor(user.asUser())
                                    now()
                                    log()
                                    roleField("Role", arguments.role)
                                }

                                respond {
                                    embed {
                                        info("Role added to `/role` roles")
                                        pinguino()
                                        now()
                                        success()
                                    }
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand(ModuleExtension::RemoveRoleArgs) {
                    name = "remove-role"
                    description =
                        "Remove a role from the list of roles that a user can apply to themselves using `/role`"

                    action {
                        val conf = guild!!.getConfig()

                        if (!conf.roleConfig.roles.containsKey(arguments.name)) {
                            respond {
                                embed {
                                    info("The list does not contain a role with that name")
                                    error()
                                    pinguino()
                                    now()
                                }
                            }
                        } else {
                            val role = conf.roleConfig.roles.remove(arguments.name)
                            database.serverConfig.updateConfig(guild!!.id, conf)

                            guild!!.getLogChannel()?.createEmbed {
                                info("Role removed from `/role` roles")
                                userAuthor(user.asUser())
                                now()
                                log()

                                if (role != null) {
                                    roleField("Role", guild!!.getRoleOrNull(Snowflake(role)))
                                } else {
                                    stringField("Role Name", arguments.name)
                                }
                            }

                            respond {
                                embed {
                                    info("Role removed from `/role` roles")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    }
                }
            }

            group("tags") {
                description = "Alter the settings of $tagsModule"

                moduleEnable("Tags") { conf ->
                    conf.tagsConfig.enabled = true
                }

                moduleDisable("Tags") { conf ->
                    conf.tagsConfig.enabled = false
                }
            }

            group("quotes") {
                description = "Alter the settings of $quotesModule"

                moduleEnable("Quotes") { conf ->
                    conf.quotesConfig.enabled = true
                }

                moduleDisable("Quotes") { conf ->
                    conf.quotesConfig.enabled = false
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel quote embeds will be sent to"

                    action {
                        val conf = guild!!.getConfig()
                        conf.quotesConfig.channel = arguments.channel.id.value.toLong()
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Quotes channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Quotes channel updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }

            group("logging") {
                description = "Alter the settings of $loggingModule"

                moduleEnable("Logging") { conf ->
                    conf.loggingConfig.enabled = true
                }

                moduleDisable("Logging") { conf ->
                    conf.loggingConfig.enabled = false
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel logging embeds will be sent to"

                    action {
                        val conf = guild!!.getConfig()
                        conf.loggingConfig.channel = arguments.channel.id.value.toLong()
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Logging channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Logging channel updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }

            group("moderation") {
                description = "Alter the settings of $moderationModule"

                moduleEnable("Moderation") { conf ->
                    conf.moderationConfig.enabled = true
                }

                moduleDisable("Moderation") { conf ->
                    conf.moderationConfig.enabled = false
                }

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "set-role"
                    description = "Set role required to run moderator level commands"

                    action {
                        val conf = guild!!.getConfig()
                        conf.moderationConfig.moderatorRole = arguments.role.id.value.toLong()
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Moderator role updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Role", arguments.role.mention)
                        }

                        respond {
                            embed {
                                info("Moderator role updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleBooleanArgs) {
                    name = "auto-save-threads"
                    description = "Set whether threads are prevented from archiving by default"

                    action {
                        val conf = guild!!.getConfig()
                        conf.moderationConfig.autoSaveThreads = arguments.boolean
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Auto-save-threads updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Value", arguments.boolean.toString())
                        }

                        respond {
                            embed {
                                info("Auto-save-threads updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-public-mod-log-channel"
                    description = "Set the channel public mod-logs will be sent to"

                    action {
                        val conf = guild!!.getConfig()
                        conf.moderationConfig.publicModLogChannel = arguments.channel.id.value.toLong()
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Public mod-log channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Public mod-log channel updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }

            group("greetings") {
                description = "Alter the settings of $notificationsModule"

                moduleEnable("Notifications") { conf ->
                    conf.notificationsConfig.enabled = true
                }

                moduleDisable("Notifications") { conf ->
                    conf.notificationsConfig.enabled = false
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel to send greetings and farewells to"

                    action {
                        val conf = guild!!.getConfig()
                        conf.notificationsConfig.greetingChannel = arguments.channel.id.value.toLong()
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Greetings channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Greetings channel updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(ModuleExtension::GreetingArgs) {
                    name = "set-greeting"
                    description = "Set the message to send to the greeting channel when a member joins"

                    action {
                        val conf = guild!!.getConfig()
                        conf.notificationsConfig.greetingMessage = arguments.string
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Greeting message updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Message", arguments.string)
                        }

                        respond {
                            embed {
                                info("Greetings message updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(ModuleExtension::GreetingArgs) {
                    name = "set-farewell"
                    description = "Set the message to send to the greeting channel when a member leaves"

                    action {
                        val conf = guild!!.getConfig()
                        conf.notificationsConfig.farewellMessage = arguments.string
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Farewell message updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Message", arguments.string)
                        }

                        respond {
                            embed {
                                info("Farewell message updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }

            group("file-paste") {
                description = "Alter the settings of $filePasteModule"

                moduleEnable("File Paste") { conf ->
                    conf.filePasteConfig.enabled = true
                }

                moduleDisable("File Paste") { conf ->
                    conf.filePasteConfig.enabled = false
                }

                ephemeralSubCommand(ModuleExtension::HastebinUrlArgs) {
                    name = "set-url"
                    description = "Set the URL to use for the Hastebin API"

                    action {
                        val url = if (arguments.url.endsWith("/")) arguments.url else "${arguments.url}/"

                        val conf = guild!!.getConfig()
                        conf.filePasteConfig.hastebinUrl = url
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Hastebin URL updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Message", arguments.url)
                        }

                        respond {
                            embed {
                                info("Hastebin URL updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }

            group("phishing") {
                description = "Alter the settings of $phishingModule"

                moduleEnable("Phishing") { conf ->
                    conf.phishingConfig.enabled = true
                }

                moduleDisable("Phishing") { conf ->
                    conf.phishingConfig.enabled = false
                }

                ephemeralSubCommand(ModuleExtension::PhishingDisciplineLevelArgs) {
                    name = "set-discipline-level"
                    description = "Set the level of discipline to use for posting a phishing link"

                    check {
                        isModuleEnabled(Modules.Phishing)
                        hasModeratorRole()
                    }

                    action {
                        val conf = guild!!.getConfig()
                        conf.phishingConfig.moderationType = arguments.level
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Phishing discipline level updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Discipline level", arguments.level.readableName)
                        }

                        respond {
                            embed {
                                info("Phishing discipline level updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleBooleanArgs) {
                    name = "set-moderators-exempt"
                    description = "Set whether moderators should be exempt from phishing checks"

                    action {
                        val conf = guild!!.getConfig()
                        conf.phishingConfig.moderatorsExempt = arguments.boolean
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Moderators exempt from phishing updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Exempt", if (arguments.boolean) "True" else "False")
                        }

                        respond {
                            embed {
                                info("Moderators exempt from phishing updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun logModuleEnabled(module: String, responsibleMod: UserBehavior, guild: GuildBehavior) {
        guild.getLogChannel()?.createEmbed {
            info("Module Enabled")
            userAuthor(responsibleMod.asUser())
            now()
            log()
            stringField("Module", module)
        }
    }

    private suspend fun logModuleDisabled(module: String, responsibleMod: UserBehavior, guild: GuildBehavior) {
        guild.getLogChannel()?.createEmbed {
            info("Module Disabled")
            userAuthor(responsibleMod.asUser())
            now()
            log()
            stringField("Module", module)
        }
    }

    class GreetingArgs : Arguments() {
        val string by string {
            name = "value"
            description = "The value - use \$user to use the username of the user in your message"
        }
    }

    class HastebinUrlArgs : Arguments() {
        val url by string {
            name = "url"
            description = "The Hastebin server to use for the file paste module. Defaults to the official Hastebin site"
        }
    }

    class PhishingDisciplineLevelArgs : Arguments() {
        val level by enum<ServerPhishingModerationType> {
            name = "level"
            description = "The level of discipline to use for posting a phishing link"
            typeName = "level"
        }
    }

    class AddRoleArgs : Arguments() {
        val role by role {
            name = "role"
            description = "The role to allow users to apply to themselves"
        }
        val name by optionalString {
            name = "name"
            description = "The name to use for the `/role add` command, or the roles name if specified"
        }
    }

    class RemoveRoleArgs : Arguments() {
        val name by string {
            name = "name"
            description = "The name of the role to remove"

            autoComplete {
                if (data.guildId.value != null) {
                    val conf = database.serverConfig.getConfig(data.guildId.value!!)
                    val map = mutableMapOf<String, String>()

                    conf.roleConfig.roles.forEach {
                        map[it.key] = it.key
                    }

                    suggestStringMap(map)
                }
            }
        }
    }
}
