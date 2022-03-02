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

package io.github.jamalam.extensions.user

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import io.github.jamalam.api.*
import io.github.jamalam.util.*
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
                        embed {
                            info("${user.asUser().username} is rolling a dice...")
                            pinguino()
                            success()
                            now()
                        }
                    }

                    channel.withTyping {
                        delay(Duration.seconds(random.nextInt(3, 8)))
                    }

                    interactionResponse.edit {
                        embed {
                            info("${user.asUser().username} rolled a ${random.nextInt(1, 7)}!")
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }

            publicSubCommand {
                name = "coin"
                description = "Flip a coin!"

                action {
                    respond {
                        embed {
                            info("${user.asUser().username} is flipping a coin...")
                            pinguino()
                            success()
                            now()
                        }
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        embed {
                            info("${user.asUser().username}'s coin landed on ${if (random.nextBoolean()) "heads" else "tails"}!")
                            pinguino()
                            success()
                            now()
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
                            info("Look at this cute dog!")
                            pinguino()
                            success()
                            now()
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
                        embed {
                            info("${user.asUser().username} is asking the magic eight ball ${arguments.question}")
                            pinguino()
                            success()
                            now()
                        }
                    }

                    channel.withTyping {
                        delay(Duration.seconds(random.nextInt(3, 8)))
                    }

                    interactionResponse.edit {
                        embed {
                            info("The magic eight ball says: ${eightBall.ask(arguments.question)}")
                            pinguino()
                            success()
                            now()
                        }
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
                            info(
                                if (arguments.comic != null) {
                                    "xkcd #${arguments.comic} - ${response.title}"
                                } else {
                                    "Today's xkcd - ${response.title}"
                                }
                            )
                            pinguino()
                            success()
                            now()
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
                        embed {
                            info("Kanye says: '${kanye.getQuote()}'")
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }

            publicSubCommand(::AgeArgs) {
                name = "age"
                description = "Predict the age of a person!"

                action {
                    respond {
                        embed {
                            info("With a name like ${arguments.name}, they must be ${age.predictAge(arguments.name)} years old!")
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }

            publicSubCommand {
                name = "bored"
                description = "Get a random activity, for when you're bored!"

                action {
                    respond {
                        embed {
                            info(bored.getActivity())
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }

            publicSubCommand {
                name = "chuck-norris-joke"
                description = "Get a random Chuck Norris joke!"

                action {
                    respond {
                        embed {
                            info(chuck.getExcellentChuckNorrisBasedJoke())
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }

            publicSubCommand {
                name = "donald"
                description = "Get a random Donald Trump quote!"

                action {
                    respond {
                        embed {
                            info("${donald.getBasedOpinionsYesThisNameIsSarcastic()} - Donald Trump")
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }

            publicSubCommand {
                name = "cat"
                description = "Get a random cat picture!"

                action {
                    respond {
                        embed {
                            info("Look at this cute cat!")
                            pinguino()
                            success()
                            now()
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
                        embed {
                            info(dad.getTheFunnyHaHa())
                            pinguino()
                            success()
                            now()
                        }
                    }
                }
            }

            publicSubCommand {
                name = "color"
                description = "Get a random color!"

                action {
                    var hexString = ""

                    @Suppress("unused")
                    for (i in 1..6) {
                        hexString += Random.Default.nextHex()
                    }

                    respond {
                        embed {
                            info("Hmm, let me have a look...")
                            pinguino()
                            success()
                            now()
                        }
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        embed {
                            info("${user.asUser().username}, I found you this color!")
                            pinguino()
                            now()

                            stringField("Hex Value", "`$hexString`")
                            color = Color(Integer.parseInt(hexString, 16))
                        }
                    }
                }
            }
        }
    }

    inner class EightBallArgs : Arguments() {
        val question by string {
            name = "question"
            description = "The question you want to ask the magic 8-ball"
        }
    }

    inner class XkcdArgs : Arguments() {
        val comic by optionalInt {
            name = "comic"
            description = "The comic you want to get, or today's comic if not specified"
        }
    }

    inner class AgeArgs : Arguments() {
        val name by string {
            name = "name"
            description = "The name of the person you want to predict the age of"

            validate {
                failIf("The argument must not be a user mention") {
                    value.contains("@")
                }
            }
        }
    }
}
