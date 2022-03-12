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

@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package io.github.jamalam.pinguino.extensions.bot

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.enum
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralSelectMenu
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.pinguino.api.HastebinApi
import io.github.jamalam.pinguino.api.TopGg
import io.github.jamalam.pinguino.config.config
import io.github.jamalam.pinguino.util.*
import kotlinx.coroutines.flow.count
import kotlinx.datetime.DateTimePeriod
import java.io.File
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */
@OptIn(ExperimentalTime::class)
class BotUtilityExtension : Extension() {
    override val name = "utility"

    private val presenceDelay = DateTimePeriod(minutes = 2, seconds = 30) // Every 2.5 minutes
    private val dblDelay = DateTimePeriod(hours = 6) // every 6 hours
    private val topGg = TopGg()
    private val hastebin = HastebinApi()
    private val logDirectory = File("./logs")
    private var presenceTask: Task? = null

    override suspend fun setup() {
        // Get the latest Pinguino PFP
        PINGUINO_PFP = kord.getUser(kord.selfId)?.avatar?.url ?: PINGUINO_PFP

        // Set initial presence (without the delay there is an error)
        presenceTask = scheduler.schedule(30) {
            this.kord.editPresence {
                status = PresenceStatus.Idle
                playing("booting up!")
            }

            setDBLStats()
        }

        scheduler.schedule(presenceDelay.toSeconds()) {
            setPresenceStatus()
        }

        ephemeralSlashCommand {
            name = "ping"
            description = "Ping the pong."

            action {
                respond {
                    embed {
                        info("Pong!")
                        pinguino()
                        success()
                        now()
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "admin"
            description = "Admin commands for Pinguino"

            guild((if (config.production()) config.production!!.adminServerId else config.development!!.serverId)!!)

            check {
                allowUser((if (config.production()) config.production!!.adminId else config.development!!.adminId)!!)
            }

            group("status") {
                description = "Commands to manage the status of Pinguino"

                ephemeralSubCommand {
                    name = "cycle"
                    description = "Cycle the status of Pinguino"

                    action {
                        setPresenceStatus()

                        respond {
                            embed {
                                info("Cycled status")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }

                ephemeralSubCommand {
                    name = "start"
                    description = "Start the status cycling"

                    action {
                        setPresenceStatus()

                        respond {
                            embed {
                                info("Started cycling status")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }

                ephemeralSubCommand {
                    name = "stop"
                    description = "Stop the status cycling"

                    action {
                        presenceTask?.cancel()

                        respond {
                            embed {
                                info("Stopped cycling status")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SetStatusArgs) {
                    name = "set"
                    description = "Set the status"

                    action {
                        this@BotUtilityExtension.kord.editPresence {
                            when (arguments.type) {
                                StatusTypeArg.Playing -> playing(arguments.message)
                                StatusTypeArg.Listening -> listening(arguments.message)
                                StatusTypeArg.Competing -> competing(arguments.message)
                                StatusTypeArg.Watching -> watching(arguments.message)
                            }
                        }

                        respond {
                            embed {
                                info("Set the bot status")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }
            }

            group("server") {
                description = "Commands to manage the servers Pinguino is in"

                ephemeralSubCommand {
                    name = "count"
                    description = "Return the number of servers Pinguino is in"

                    action {
                        respond {
                            embed {
                                info("Pinguino is in ${this@BotUtilityExtension.kord.guilds.count()} servers")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }
            }

            group("announcements") {
                description = "Commands to manage bot announcements"

                ephemeralSubCommand {
                    name = "count"
                    description = "Return the number of announcement subscribers"

                    action {
                        respond {
                            embed {
                                info(
                                    "Pinguino has ${database.announcementSubscribers.getSubscribers().size}" +
                                            " announcement subscribers"
                                )
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }
            }

            group("logs") {
                description = "Commands to get log files"

                ephemeralSubCommand {
                    name = "upload"
                    description = "Upload the log file as an attachment"

                    action {
                        logDirectory.listFiles()

                        respond {
                            embed {
                                info("Select desired log file")
                                pinguino()
                                success()
                                now()
                            }

                            components {
                                ephemeralSelectMenu {
                                    maximumChoices = 1

                                    logDirectory.listFiles().forEach {
                                        options.add(SelectOptionBuilder(it.nameWithoutExtension, it.name))
                                    }

                                    options.forEach {
                                        if (it.label.contains("latest")) {
                                            val itIndex = options.indexOf(it)
                                            val atZero = options[0]

                                            options[0] = it
                                            options[itIndex] = atZero
                                        }
                                    }

                                    action {
                                        respond {
                                            embed {
                                                info("Here is the log file")
                                                success()
                                                pinguino()
                                                now()
                                            }

                                            addFile(File("${logDirectory.path}/${selected[0]}").toPath())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ephemeralSubCommand {
                    name = "hastebin"
                    description = "Upload the log file to Hastebin"

                    action {
                        logDirectory.listFiles()

                        respond {
                            embed {
                                info("Select desired log file")
                                pinguino()
                                success()
                                now()
                            }

                            components {
                                ephemeralSelectMenu {
                                    maximumChoices = 1

                                    logDirectory.listFiles().forEach {
                                        options.add(SelectOptionBuilder(it.nameWithoutExtension, it.name))
                                    }

                                    options.forEach {
                                        if (it.label.contains("latest")) {
                                            val itIndex = options.indexOf(it)
                                            val atZero = options[0]

                                            options[0] = it
                                            options[itIndex] = atZero
                                        }
                                    }

                                    action {
                                        respond {
                                            embed {
                                                info("Here is the log file")
                                                success()
                                                pinguino()
                                                now()

                                                val file = File("${logDirectory.path}/${selected[0]}")
                                                url = "https://www.toptal.com/developers/hastebin/${
                                                    hastebin.paste(
                                                        "https://www.toptal.com/developers/hastebin/",
                                                        file.readText()
                                                    )
                                                }"
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

    private suspend fun setPresenceStatus() {
        BotStatus.random().setPresenceStatus(this.kord)
        presenceTask?.cancel()
        presenceTask = scheduler.schedule(presenceDelay.toSeconds()) {
            setPresenceStatus()
        }
    }

    private suspend fun setDBLStats() {
        if (config.production()) {
            topGg.sendServerCount(kord.guilds.count())

            scheduler.schedule(dblDelay.toSeconds()) {
                setDBLStats()
            }
        }
    }

    inner class SetStatusArgs : Arguments() {
        val type by enum<StatusTypeArg> {
            name = "type"
            typeName = "type"
            description = "The type of status to set"

            autoComplete {
                suggestStringMap(
                    mapOf(
                        Pair("Playing", "Playing"),
                        Pair("Listening", "Listening"),
                        Pair("Competing", "Competing"),
                        Pair("Watching", "Watching")
                    )
                )
            }
        }
        val message by string {
            name = "message"
            description = "The message to set"
        }
    }
}

enum class StatusTypeArg(override val readableName: String) : ChoiceEnum {
    Playing("Playing"),
    Listening("Listening"),
    Competing("Competing"),
    Watching("Watching");
}

@Suppress("unused")
enum class BotStatus(val setPresenceStatus: suspend (kord: Kord) -> Unit) {
    ServerCount({
        it.editPresence {
            status = PresenceStatus.Online
            watching("over ${it.guilds.count()} servers")
        }
    }),
    CurrentVersion({
        it.editPresence {
            status = PresenceStatus.Online
            playing("Pinguino $VERSION")
        }
    }),
    RandomFun({
        it.editPresence {
            status = PresenceStatus.Online

            var status = RandomStatus.random()
            val canBeUptime = it.getUptime().minutes > 5

            while (!canBeUptime && status.message.contains("%UPTIME%")) {
                status = RandomStatus.random()
            }

            var message = status.message

            if (message.contains("%UPTIME%")) {
                message = message.replace("%UPTIME%", it.getUptime().toPrettyString())
            }

            when (status.type) {
                StatusType.Watching -> watching(message)
                StatusType.Playing -> playing(message)
                StatusType.Listening -> listening(message)
                StatusType.Competing -> competing(message)
            }
        }
    });

    companion object {
        private val values = values()
        private val size = values.size

        fun random(): BotStatus = values[Random.nextInt(size)]
    }
}

@Suppress("unused")
enum class RandomStatus(val type: StatusType, val message: String) {
    ListeningForYourCommands(StatusType.Listening, "your commands"),
    ListeningToDawnFm(StatusType.Listening, "103.5, DawnFM"),
    ListeningToTdcc(StatusType.Listening, "Two Door Cinema Club"),
    WatchingForYourCommands(StatusType.Watching, "for your commands"),
    WatchingTheFootball(StatusType.Watching, "the football"),
    WatchingYou(StatusType.Watching, "you"),
    WatchingTheWorldBurn(StatusType.Watching, "the world burn"),
    WatchingOverYourServer(StatusType.Watching, "over your server"),
    WatchingForScammers(StatusType.Watching, "for scammers"),
    WatchingTv(StatusType.Watching, "TV"),
    WatchingForUptime(StatusType.Watching, "for %UPTIME%"),
    PlayingMinecraft(StatusType.Playing, "Minecraft :D"),
    PlayingABoardGame(StatusType.Playing, "a board game"),
    PlayingThePiano(StatusType.Playing, "the piano"),
    PlayingForUptime(StatusType.Playing, "for %UPTIME%"),
    CompetingInTheOlympics(StatusType.Competing, "the olympics"),
    CompetingWithOtherPenguins(StatusType.Competing, "the international penguin tournament");

    companion object {
        private val values = values()
        private val size = values.size

        fun random(): RandomStatus = values[Random.nextInt(size)]
    }
}

enum class StatusType {
    Watching,
    Playing,
    Listening,
    Competing
}
