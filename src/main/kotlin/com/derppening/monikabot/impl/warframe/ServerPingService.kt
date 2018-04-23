/*
 *  This file is part of MonikaBot.
 *
 *  Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 *  MonikaBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MonikaBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.derppening.monikabot.impl.warframe

import com.derppening.monikabot.core.ILogger
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis

object ServerPingService : ILogger {
    fun getPingEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            withTitle("Warframe Latency Information")

            PingDestination.values().forEach { (server, url, expectedResponse) ->
                var responseCode = 0
                logger.info("Pinging $server at $url...")
                val time = measureTimeMillis {
                    val connection = URL(url).openConnection().also {
                        it.connectTimeout = 10000
                        it.connect()
                    }
                    if (connection is HttpURLConnection) {
                        responseCode = connection.responseCode
                    }
                }
                logger.info("Connecting to $url took $time ms, with response code $responseCode")

                val isResponseExpected = expectedResponse.any { it == responseCode }
                appendField(server, if (time < 10000 && isResponseExpected) "$time ms" else "Unreachable", false)
            }
        }.build()
    }

    enum class PingDestination(val url: String, val expectedResponse: List<Int>) {
        INTERNAL_API("https://api.warframe.com/stats/view.php", listOf(403)),
        CONTENT_SERVER("http://content.warframe.com/dynamic/worldState.php", listOf(200)),
        FORUMS("https://forums.warframe.com/", listOf(200)),
        WEB_SERVER("https://n8k6e2y6.ssl.hwcdn.net/repos/hnfvc0o3jnfvc873njb03enrf56.html", listOf(200));

        override fun toString(): String {
            return name.replace("_", " ").toLowerCase().capitalize()
        }

        operator fun component1(): String = toString()
        operator fun component2(): String = url
        operator fun component3(): List<Int> = expectedResponse
    }
}