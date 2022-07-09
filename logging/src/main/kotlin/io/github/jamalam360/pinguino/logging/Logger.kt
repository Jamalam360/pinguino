package io.github.jamalam360.pinguino.logging

import mu.KLogger
import mu.KotlinLogging

/**
 * @author  Jamalam
 */

fun getLogger(module: String, name: String): KLogger = KotlinLogging.logger("$module | $name")
