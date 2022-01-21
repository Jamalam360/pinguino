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

package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam360.api.*
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class, ExperimentalTime::class)
class FunExtension : Extension() {
    override val name: String = "fun"

    private val random = Random.Default
    private val dog = DogApi()
    private val eightBall = EightBallApi()
    private val xkcd = XkcdApi()
    private val kanye = KanyeApi()
    private val age = AgeApi()
    private val bored = BoredApi()
    private val chuck = ChuckNorrisApi()
    private val donald = DonaldApi()
    private val cat = CatApi()
    private val dad = DadJokeApi()

    override suspend fun setup() {
        publicSlashCommand {
            name = "fun"
            description = "A collection of fun commands!"

            publicSubCommand {
                name = "dice"
                description = "Roll a dice!"

                action {
                    respond {
                        content = "${user.mention} is rolling a dice!"
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        content = "${user.mention} rolled a ${random.nextInt(1, 7)}!"
                    }
                }
            }

            publicSubCommand {
                name = "coin"
                description = "Flip a coin!"

                action {
                    respond {
                        content = "${user.mention} is flipping a coin!"
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        content = if (random.nextBoolean()) {
                            "It's heads!"
                        } else {
                            "It's tails!"
                        }
                    }
                }
            }

            publicSubCommand {
                name = "dog"
                description = "Get a random photo of a dog!"

                action {
                    respond {
                        embed {
                            title = "Look at this cute dog!"
                            image = dog.getRandomDog()
                        }
                    }
                }
            }

            publicSubCommand(::EightBallArgs) {
                name = "eight-ball"
                description = "Ask the magic 8-ball a question!"

                action {
                    respond {
                        content = "Asking the magic eight ball ${arguments.question}"
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        content = "The magic eight ball answered: ${eightBall.ask(arguments.question)}"
                    }
                }
            }

            publicSubCommand(::XkcdArgs) {
                name = "xkcd"
                description = "Get an xkcd comic!"

                action {
                    val response =
                        if (arguments.comic != null) xkcd.getComic(arguments.comic!!) else xkcd.getLatestComic()
                    respond {
                        embed {
                            title = if (arguments.comic != null) {
                                "xkcd #${arguments.comic} - ${response.title}"
                            } else {
                                "Today's xkcd - ${response.title}"
                            }

                            description = response.alt
                            image = response.img
                        }
                    }
                }
            }

            publicSubCommand {
                name = "kanye"
                description = "Kanye as a service (send help)"

                action {
                    respond {
                        content = "'${kanye.getQuote()}' - Kanye West"
                    }
                }
            }

            publicSubCommand(::AgeArgs) {
                name = "age"
                description = "Predict the age of a person!"

                action {
                    respond {
                        content =
                            "With a name like ${arguments.name}, they must be ${age.predictAge(arguments.name)} years old!"
                    }
                }
            }

            publicSubCommand {
                name = "bored"
                description = "Get a random activity, for when you're bored!"

                action {
                    respond {
                        content = bored.getActivity()
                    }
                }
            }

            publicSubCommand {
                name = "chuck-norris-joke"
                description = "Get a random Chuck Norris joke!"

                action {
                    respond {
                        content = chuck.getExcellentChuckNorrisBasedJoke()
                    }
                }
            }

            publicSubCommand {
                name = "donald"
                description = "Get a random Donald Trump quote!"

                action {
                    respond {
                        content = "'${donald.getBasedOpinionsYesThisNameIsSarcastic()}' - Donald Trump"
                    }
                }
            }

            publicSubCommand {
                name = "cat"
                description = "Get a random cat picture!"

                action {
                    respond {
                        embed {
                            title = "Look at this cute cat!"
                            image = cat.getRandomCat()
                        }
                    }
                }
            }

            publicSubCommand {
                name = "dad-joke"
                description = "Get a random dad joke!"

                action {
                    respond {
                        content = dad.getTheFunnyHaHa()
                    }
                }
            }
        }
    }

    inner class EightBallArgs : Arguments() {
        val question by string(
            "question",
            "The question you want to ask the magic 8-ball",
        )
    }

    inner class XkcdArgs : Arguments() {
        val comic by optionalInt(
            "comic",
            "The comic you want to get, or today's comic if not specified",
        )
    }

    inner class AgeArgs : Arguments() {
        val name by string(
            "name",
            "The name of the person you want to predict the age of",
        )
    }
}

