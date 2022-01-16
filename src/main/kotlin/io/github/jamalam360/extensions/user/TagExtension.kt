package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import io.github.jamalam360.DATABASE
import io.github.jamalam360.hasModeratorRole
import io.github.jamalam360.util.getLoggingExtension

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

            publicSubCommand(::TagNameArgs) {
                name = "use"
                description = "Use a tag"

                action {
                    val conf = DATABASE.config.getConfig(guild!!.id)

                    if (conf.tagsConfig.tags[arguments.name.lowercase()] != null) {
                        respond {
                            content = conf.tagsConfig.tags[arguments.name.lowercase()]
                        }
                    } else {
                        respond {
                            content = "Cannot find that tag"
                        }
                    }
                }
            }

            ephemeralSubCommand {
                name = "list"
                description = "List all available tags"

                action {
                    val conf = DATABASE.config.getConfig(guild!!.id)
                    var response = ""

                    if (conf.tagsConfig.tags.isEmpty()) {
                        response = "No tags created!"
                    }

                    conf.tagsConfig.tags.forEach {
                        response += "`${it.key}`"
                        response += "\n"
                    }

                    respond {
                        content = response
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
                    val conf = DATABASE.config.getConfig(guild!!.id)

                    if (conf.tagsConfig.tags.size < 50) {
                        if (conf.tagsConfig.tags[arguments.name.lowercase()] != null) {
                            respond {
                                content = "There is already a tag with the name `${arguments.name}`"
                            }
                        } else {
                            conf.tagsConfig.tags[arguments.name.lowercase()] = arguments.content
                            DATABASE.config.updateConfig(guild!!.id, conf)

                            respond {
                                content = "Successfully created tag `${arguments.name}`"
                            }

                            bot.getLoggingExtension().logAction(
                                "Tag Created",
                                arguments.name,
                                user.asUser(),
                                guild!!.asGuild()
                            )
                        }
                    } else {
                        respond {
                            content = "Cannot create more than 50 tags!"
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
                    val conf = DATABASE.config.getConfig(guild!!.id)

                        if (conf.tagsConfig.tags[arguments.name.lowercase()] == null) {
                            respond {
                                content = "There is not a tag with the name `${arguments.name}`"
                            }
                        } else {
                            conf.tagsConfig.tags.remove(arguments.name.lowercase())
                            DATABASE.config.updateConfig(guild!!.id, conf)

                            respond {
                                content = "Successfully deleted tag `${arguments.name}`"
                            }

                            bot.getLoggingExtension().logAction(
                                "Tag Deleted",
                                arguments.name,
                                user.asUser(),
                                guild!!.asGuild()
                            )
                        }
                    }
            }
        }
    }

    //region Arguments
    inner class TagNameArgs : Arguments() {
        val name by string(
            "name",
            "The name of the tag"
        )
    }

    inner class TagCreateArgs : Arguments() {
        val name by string(
            "name",
            "The name of the tag"
        )
        val content by string(
            "content",
            "The content of the tag"
        )
    }
    //endregion
}
