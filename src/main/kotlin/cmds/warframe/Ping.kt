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

package cmds.warframe

import cmds.IBase
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis

object Ping : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        event.channel.typingStatus = true
        buildEmbed(event.channel) {
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
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-ping`")
            withDesc("Displays the current latency of the bot to various Warframe servers.")
            insertSeparator()
            appendField("Usage", "```warframe ping```", false)
            appendField("Internal API", "The servers responsible for loading and updating player progress.", false)
            appendField("Content Server", "The servers responsible for hosting updates and world information.", false)
            appendField("Forums", "The Warframe Forums.", false)
            appendField("Web Server", "Warframe's website, including drop tables.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
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