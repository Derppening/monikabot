package cmds.warframe

import cmds.IBase
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL
import kotlin.system.measureTimeMillis

object Ping : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        event.channel.toggleTypingStatus()
        buildEmbed(event.channel) {
            withTitle("Warframe Latency Information")

            connections.forEach { (server, url) ->
                val time = measureTimeMillis {
                    URL(url).openConnection().also {
                        it.connectTimeout = 10000
                    }.connect()
                }


                appendField(server, if (time < 10000) "$time ms" else "Unreachable", false)
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
            appendField("Content Server", "The servers responsible for publishing public information, including World State.", false)
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

    private val connections = mapOf(
            "Internal API" to "https://api.warframe.com/stats/view.php",
            "Content Server" to "http://content.warframe.com/dynamic/worldState.php",
            "Forums" to "http://forums.warframe.com/",
            "Web Server" to "https://n8k6e2y6.ssl.hwcdn.net/repos/hnfvc0o3jnfvc873njb03enrf56.html"
    )
}