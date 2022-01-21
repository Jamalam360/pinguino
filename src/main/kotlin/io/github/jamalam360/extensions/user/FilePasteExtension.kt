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

@file:OptIn(ExperimentalTime::class)

package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam360.Modules
import io.github.jamalam360.api.HastebinApi
import io.github.jamalam360.database.entity.ServerConfig
import io.github.jamalam360.util.*
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class FilePasteExtension : Extension() {
    override val name = "file-paste"

    private val hasteBin = HastebinApi()

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                isModuleEnabled(Modules.FilePaste)
            }

            action {
                if (event.member == null || event.message.attachments.isEmpty()) {
                    return@action
                }

                val conf: ServerConfig = database.config.getConfig(event.member!!.guildId)

                event.message.attachments.forEach {
                    if (!it.isImage && (it.url.endsWith(".txt") || it.url.endsWith(".log"))) {
                        event.message.channel.createMessage {
                            allowedMentions {
                                repliedUser = true
                            }

                            messageReference = event.message.id

                            embed {
                                info("Upload File to Hastebin?")
                                now()
                                pinguino()
                                success()
                            }

                            components {
                                publicButton {
                                    label = "Yes"

                                    action {
                                        hasteBin.pasteFromCdn(conf.filePasteConfig.hastebinUrl, it.url)
                                            .let { hastebinApiResponse ->
                                                respond {
                                                    embed {
                                                        info("File Uploaded to Hastebin")
                                                        now()
                                                        pinguino()
                                                        success()
                                                        url =
                                                            "${conf.filePasteConfig.hastebinUrl}${hastebinApiResponse}"
                                                    }
                                                }

                                                guild!!.getLogChannel()?.createEmbed {
                                                    info("File Uploaded to Hastebin")
                                                    userAuthor(member!!.asUser())
                                                    now()
                                                    success()
                                                    url =
                                                        "${conf.filePasteConfig.hastebinUrl}${hastebinApiResponse}"
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
