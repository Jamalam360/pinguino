package io.github.jamalam360.util

import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake

/**
 * @author  Jamalam360
 */

val PRODUCTION = env("PRODUCTION").toBoolean()

val TEST_SERVER_ID = if (PRODUCTION) {
    Snowflake(0)
} else {
    Snowflake(
        env("TEST_SERVER_ID").toLong()
    )
}

val TOKEN = if (PRODUCTION) {
    env("TOKEN")
} else {
    env("TEST_BOT_TOKEN")
}

val DBL_TOKEN = env("DBL_TOKEN")
val SENTRY_URL = env("SENTRY_URL")
val MONGO_SRV_URL = env("MONGO_SRV_URL")