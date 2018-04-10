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

package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.insertSeparator
import core.Client
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Instant

object Ping : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        event.channel.typingStatus = true
        buildEmbed(event.channel) {
            withTitle("Bot Latency Information")

            appendField("Discord Server", "${Client.shards.first().responseTime}ms", false)

            digitalOceanPings.entries.joinToString("\n") { (server, ip) ->
                val p = ProcessBuilder("ping -c 1 $ip".split(" "))
                val process = p.start()
                process.waitFor()

                val str = process.inputStream.bufferedReader().readText()
                val time = Regex("time=((?:[\\d.])+ ms)").find(str)?.groups?.get(1)?.value ?: ""

                if (time.isNotBlank()) {
                    "$server: $time"
                } else {
                    "$server: Unreachable"
                }
            }.also {
                appendField("DigitalOcean Servers", it, false)
            }

            dnsPings.entries.joinToString("\n") { (server, ip) ->
                val p = ProcessBuilder("ping -c 1 $ip".split(" "))
                val process = p.start()
                process.waitFor()

                val str = process.inputStream.bufferedReader().readText()
                val time = Regex("time=((?:[\\d.])+ ms)").find(str)?.groups?.get(1)?.value ?: ""

                if (time.isNotBlank()) {
                    "$server: $time"
                } else {
                    "$server: Unreachable"
                }
            }.also {
                appendField("DNS Servers", it, false)
            }

            withTimestamp(Instant.now())
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `ping`")
            withDesc("Displays the current latency of the bot to various servers.")
            insertSeparator()
            appendField("Usage", "```ping```", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    private val digitalOceanPings = mapOf(
            "New Jersey" to "104.236.17.211",
            "Singapore" to "128.199.105.91",
            "Germany" to "165.227.164.4",
            "Netherlands" to "188.166.23.150",
            "California" to "192.184.13.42"
    )

    private val dnsPings = mapOf(
            "Google" to "8.8.8.8",
            "CloudFlare" to "1.1.1.1",
            "Quad9" to "9.9.9.9",
            "OpenDNS" to "208.67.222.222",
            "Yandex DNS" to "77.88.8.7",
            "Comodo DNS" to "8.26.56.26"
    )
}