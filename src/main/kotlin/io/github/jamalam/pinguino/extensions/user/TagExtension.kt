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

package io.github.jamalam.pinguino.extensions.user

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.pinguino.Modules
import io.github.jamalam.pinguino.util.*

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class TagExtension : Extension() {
    override val name: String = "tags"

    override suspend fun setup() {
        publicSlashCommand {
            name = "tag"
            description = "Manage or use tags saved on this server"

            check {
                isModuleEnabled(Modules.Tags)
                notInDm()
            }

            publicSubCommand(::TagNameArgs) {
                name = "use"
                description = "Use a tag"

                action {
                    val conf = database.serverConfig.getConfig(guild!!.id)

                    if (conf.tagsConfig.tags[arguments.name] != null) {
                        respond {
                            embed {
                                description = conf.tagsConfig.tags[arguments.name]!!
                                pinguino()
                                now()
                                success()

                                footer {
                                    text = "Tag ${arguments.name} requested by ${user.asUser().username}"
                                }
                            }
                        }
                    } else {
                        respond {
                            embed {
                                info("Cannot find that tag")
                                pinguino()
                                now()
                                error()
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand {
                name = "list"
                description = "List all available tags"

                action {
                    val conf = database.serverConfig.getConfig(guild!!.id)
                    var response = ""

                    if (conf.tagsConfig.tags.isEmpty()) {
                        response = "No tags created!"
                    }

                    conf.tagsConfig.tags.forEach {
                        response += "`${it.key}`"
                        response += "\n"
                    }

                    respond {
                        embed {
                            info(response)
                            pinguino()
                            now()
                            success()
                        }
                    }
                }
            }

            ephemeralSubCommand(::TagCreateArgs) {
                name = "create"
                description = "Create a tag, if you are a moderator"

                check {
                    hasModeratorRole()
                }

                action {
                    val conf = database.serverConfig.getConfig(guild!!.id)

                    if (conf.tagsConfig.tags.size < 50) {
                        if (conf.tagsConfig.tags[arguments.name] != null) {
                            respond {
                                embed {
                                    info("There is already a tag with that name")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else if (arguments.content.length > 2000) {
                            respond {
                                embed {
                                    info("Tag content must be under 2000 characters long")
                                    pinguino()
                                    now()
                                    error()
                                }
                            }
                        } else {
                            conf.tagsConfig.tags[arguments.name] = arguments.content
                            database.serverConfig.updateConfig(guild!!.id, conf)

                            guild!!.getLogChannel()?.createEmbed {
                                info("Tag created")
                                userAuthor(user.asUser())
                                now()
                                log()
                                stringField("Tag Name", arguments.name)
                                stringField("Tag Content", arguments.content)
                            }

                            respond {
                                embed {
                                    info("Tag created")
                                    pinguino()
                                    now()
                                    success()
                                }
                            }
                        }
                    } else {
                        respond {
                            embed {
                                info("Cannot create more than 50 tags")
                                pinguino()
                                now()
                                error()
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand(::TagEditArgs) {
                name = "edit"
                description = "Edit a tag, if you are a moderator"

                check {
                    hasModeratorRole()
                }

                action {
                    val conf = database.serverConfig.getConfig(guild!!.id)

                    if (conf.tagsConfig.tags[arguments.name] == null) {
                        respond {
                            embed {
                                info("There is not a tag with that name")
                                pinguino()
                                now()
                                error()
                            }
                        }
                    } else if (arguments.content.length > 2000) {
                        respond {
                            embed {
                                info("Tag content must be under 2000 characters long")
                                pinguino()
                                now()
                                error()
                            }
                        }
                    } else {
                        val contentBefore = conf.tagsConfig.tags[arguments.name]
                        conf.tagsConfig.tags[arguments.name] = arguments.content
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Tag edited")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Tag Name", arguments.name)
                            stringField("Tag Content (Before)", contentBefore)
                            stringField("Tag Content (After)", arguments.content)
                        }

                        respond {
                            embed {
                                info("Tag created")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand(::TagNameArgs) {
                name = "delete"
                description = "Delete a tag, if you are a moderator"

                check {
                    hasModeratorRole()
                }

                action {
                    val conf = database.serverConfig.getConfig(guild!!.id)

                    if (conf.tagsConfig.tags[arguments.name] == null) {
                        respond {
                            embed {
                                info("There is not a tag with that name")
                                pinguino()
                                now()
                                error()
                            }
                        }
                    } else {
                        val content = conf.tagsConfig.tags[arguments.name]
                        conf.tagsConfig.tags.remove(arguments.name)
                        database.serverConfig.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Tag deleted")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Tag Name", arguments.name)
                            stringField("Tag Content", content)
                        }

                        respond {
                            embed {
                                info("Tag deleted")
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

    open inner class TagNameArgs : Arguments() {
        val name by string {
            name = "name"
            description = "The name of the tag"

            autoComplete {
                if (data.guildId.value != null) {
                    val conf = database.serverConfig.getConfig(data.guildId.value!!)
                    val map = mutableMapOf<String, String>()

                    conf.tagsConfig.tags.forEach {
                        map[it.key] = it.key
                    }

                    suggestStringMap(map)
                }
            }
        }
    }

    inner class TagCreateArgs : Arguments() {
        val name by string {
            name = "name"
            description = "The name of the tag"
        }
        val content by string {
            name = "content"
            description = "The content of the tag"
        }
    }

    inner class TagEditArgs : TagNameArgs() {
        val content by string {
            name = "content"
            description = "The content of the tag"
        }
    }
}
