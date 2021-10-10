package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import kotlinx.coroutines.flow.count

/**
 * @author  Jamalam360
 */
class BotStatusExtension : Extension() {
    override val name = "status"
    private val scheduler = Scheduler()

    override suspend fun setup() {
        event<GuildCreateEvent> {
            action {
                editPresence(event.kord)
            }
        }

        event<GuildDeleteEvent> {
            action {
                editPresence(event.kord)
            }
        }

        scheduler.schedule(10, true, "UpdateInitialStatus", 1) {
            editPresence(this.kord)
        }
    }

    private suspend fun editPresence(kord: Kord) {
        kord.editPresence {
            status = PresenceStatus.Online
            watching("over ${kord.guilds.count()} servers")
        }
    }
}
