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

package io.github.jamalam.extensions.bot

import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.api.TopGg
import io.github.jamalam.util.*
import kotlinx.coroutines.flow.count
import kotlinx.datetime.DateTimePeriod
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */
@OptIn(ExperimentalTime::class)
class BotUtilityExtension : Extension() {
    override val name = "utility"

    private val presenceDelay = DateTimePeriod(minutes = 2, seconds = 30) //Every 2.5 minutes
    private val dblDelay = DateTimePeriod(minutes = 30) //Every 10 minutes
    private val topGg = TopGg()
    private var presenceTask: Task? = null

    override suspend fun setup() {
        // Set initial presence (without the delay there is an error)
        presenceTask = scheduler.schedule(30) {
            this.kord.editPresence {
                status = PresenceStatus.Idle
                playing("booting up!")
            }

            setDBLStats()
        }

        scheduler.schedule(presenceDelay.seconds.toLong()) {
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

            guild(if (PRODUCTION) ADMIN_SERVER_ID else TEST_SERVER_ID)

            check {
                allowUser(ADMIN_ID)
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
            }
        }
    }

    private suspend fun setPresenceStatus() {
        BotStatus.random().setPresenceStatus(this.kord)
        presenceTask?.cancel()
        presenceTask = scheduler.schedule(seconds = presenceDelay.seconds.toLong()) {
            setPresenceStatus()
        }
    }

    private suspend fun setDBLStats() {
        if (PRODUCTION) {
            topGg.sendServerCount(kord.guilds.count())

            scheduler.schedule(seconds = dblDelay.seconds.toLong()) {
                setDBLStats()
            }
        }
    }
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
    ListeningToDawnFm(StatusType.Listening, "to 103.5, DawnFM"),
    ListeningToTdcc(StatusType.Listening, "to Two Door Cinema Club"),
    ListeningForUptime(StatusType.Listening, "for %UPTIME%"),
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
