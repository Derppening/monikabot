package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.Client
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import java.time.Instant

object Ping : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        val message = buildMessage(event.channel) {
            withContent("Pinging servers, please wait...")
        }

        val embed = EmbedBuilder().apply {
            withTitle("Bot Latency Information")

            appendField("Discord Server", "${Client.shards.first().responseTime}ms", false)

            val digitalOceanField = digitalOceanPings.entries.joinToString("\n") { (server, ip) ->
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
            }
            appendField("DigitalOcean Servers", digitalOceanField, false)

            val googleField = googlePings.entries.joinToString("\n") { (server, ip) ->
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
            }
            appendField("Google Servers", googleField, false)

            withTimestamp(Instant.now())
        }.build()

        message?.edit(embed)

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

    private val googlePings = mapOf(
            "Primary DNS" to "8.8.8.8",
            "Secondary DNS" to "8.8.4.4",
            "Local Server" to "www.google.com"
    )
}