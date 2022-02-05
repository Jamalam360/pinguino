package io.github.jamalam.extensions.moderation

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.modules.extra.phishing.DomainChangeType
import com.kotlindiscord.kord.extensions.modules.extra.phishing.PhishingApi
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.Modules
import io.github.jamalam.util.*
import kotlin.time.ExperimentalTime

/*
* This was modified from the KordEx phishing module by Jamalam. The original file was written by gdude2002.
*/

/** Phishing extension, responsible for checking for phishing domains in messages. **/
@Suppress("StringLiteralDuplication")
@OptIn(ExperimentalTime::class)
class PhishingExtension : Extension() {
    override val name = "phishing"

    private val api = PhishingApi("Pinguino")
    private val domainCache: MutableSet<String> = mutableSetOf()

    private val scheduler = Scheduler()
    private var checkTask: Task? = null

    override suspend fun setup() {
        domainCache.addAll(api.getAllDomains())

        checkTask = scheduler.schedule(15, pollingSeconds = 30, callback = ::updateDomains)

        event<MessageCreateEvent> {
            check {
                notHasModeratorRole()
                isNotBot()
                isModuleEnabled(Modules.Phishing)
            }

            action {
                handleMessage(event.message)
            }
        }

        event<MessageUpdateEvent> {
            check {
                notHasModeratorRole()
                isNotBot()
                isModuleEnabled(Modules.Phishing)
            }

            action {
                handleMessage(event.message.asMessage())
            }
        }

        ephemeralMessageCommand {
            name = "Phishing Check"

            check {
                isModuleEnabled(Modules.Phishing)
            }

            action {
                for (message in targetMessages) {
                    val domains = parseDomains(message.content.lowercase())
                    val matches = domains intersect domainCache

                    respond {
                        if (matches.isNotEmpty()) {
                            embed {
                                info("That message contains phishing links!")
                                pinguino()
                                error()
                                now()
                                stringField("Phishing Links", matches.joinToString("\n"))
                            }
                        } else {
                            embed {
                                info("That message does not contain phishing links")
                                pinguino()
                                success()
                                now()
                            }
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand(::DomainArgs) {
            name = "phishing-check"
            description = "Check whether a given domain is a known phishing domain."

            check {
                isModuleEnabled(Modules.Phishing)
            }

            action {
                respond {
                    if (domainCache.contains(arguments.domain.lowercase())) {
                        embed {
                            info("That URL is a phishing domain!")
                            pinguino()
                            error()
                            now()
                        }
                    } else {
                        embed {
                            info("That URL is not a phishing domain")
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleMessage(message: Message) {
        val domains = parseDomains(message.content.lowercase())
        val matches = domains intersect domainCache

        if (matches.isNotEmpty()) {
            message.author!!.dm {
                embed {
                    info("Phishing domain detected")
                    pinguino()
                    error()
                    now()

                    stringField("Guild", message.getGuild().name)
                    channelField("Channel", message.channel.asChannel())
                    stringField("Phishing Links", matches.joinToString("\n"))
                }
            }

            message.delete("Message contained a phishing domain")
            logDeletion(message, matches)
        }
    }

    private suspend fun logDeletion(message: Message, matches: Set<String>) {
        message.getGuild().getLogChannel()?.createEmbed {
            info("Phishing domain detected")
            pinguino()
            error()
            now()
            userField("User", message.author!!.asUser())
            channelField("Channel", message.channel.asChannel())
            stringField("Content", message.content)
            stringField("Phishing Links", matches.joinToString("\n"))
        }
    }

    private fun parseDomains(content: String): MutableSet<String> {
        val domains: MutableSet<String> = mutableSetOf()

        for (match in "(?:https?|ftp|file|discord)://([-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])"
            .toRegex(RegexOption.IGNORE_CASE).findAll(content)) {
            var found = match.groups[1]!!.value.trim('/')

            if ("/" in found) {
                found = found.split("/", limit = 2).first()
            }

            domains.add(found)
        }

        return domains
    }

    @Suppress("MagicNumber")
    internal suspend fun updateDomains() {
        api.getRecentDomains(15 * 60 + 30).forEach {
            when (it.type) {
                DomainChangeType.Add -> domainCache.addAll(it.domains)
                DomainChangeType.Delete -> domainCache.removeAll(it.domains)
            }
        }

        checkTask?.restart()
    }

    /** Arguments class for domain-relevant commands. **/
    inner class DomainArgs : Arguments() {
        /** Targeted domain string. **/
        val domain by string {
            name = "domain"
            description = "The domain to check"

            validate {
                if ("/" in value) {
                    throw DiscordRelayedException("Please provide the domain name only, without the protocol or a path.")
                }
            }
        }
    }
}