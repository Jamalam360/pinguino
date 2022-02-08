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

package io.github.jamalam.util

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
val ADMIN_ID = Snowflake(env("ADMIN_ID"))
val ADMIN_SERVER_ID = Snowflake(env("ADMIN_SERVER_ID"))