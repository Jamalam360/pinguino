package io.github.jamalam360.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.jamalam360.extensions.moderation.LoggingExtension

/**
 * @author  Jamalam360
 */

fun ExtensibleBot.getLoggingExtension(): LoggingExtension = this.extensions["logging"] as LoggingExtension