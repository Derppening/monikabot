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
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        event.channel.typingStatus = true
        buildEmbed(event.channel) {
            withTitle("Warframe Latency Information")

            connections.forEach { (server, url, expectedResponse) ->
                var responseCode = 0
                val time = measureTimeMillis {
                    val connection = URL(url).openConnection().also {
                        it.connectTimeout = 10000
                        it.connect()
                    }
                    if (connection is HttpURLConnection) {
                        responseCode = connection.responseCode
                    }
                }

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

    private val connections = listOf(
            PingDestination("Internal API", "https://api.warframe.com/stats/view.php", listOf(403)),
            PingDestination("Content Server", "http://content.warframe.com/dynamic/worldState.php", listOf(200)),
            PingDestination("Forums", "https://forums.warframe.com/", listOf(200)),
            PingDestination("Web Server", "https://n8k6e2y6.ssl.hwcdn.net/repos/hnfvc0o3jnfvc873njb03enrf56.html", listOf(200))
    )

    data class PingDestination(
            val name: String,
            val url: String,
            val expectedResponse: List<Int>
    )
}