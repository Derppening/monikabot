package cmds.experimental

import cmds.IBase
import cmds.experimental.warframe.Market
import cmds.experimental.warframe.News
import cmds.experimental.warframe.WorldState
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.*
import core.BuilderHelper.buildEmbed
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.net.URL
import kotlin.concurrent.timer
import kotlin.system.measureTimeMillis

object Warframe : IBase, IChannelLogger, IConsoleLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content)
                .toMutableList()
                .apply {
                    removeIf {
                        it =="--experimental"
                    }
                }.toList()

        if (args.isEmpty()) {
            return Parser.HandleState.NOT_FOUND
        }

        return when (args[0]) {
            "news" -> News.handler(event)
            "market" -> Market.handler(event)
            else -> {
                help(event, false)
                Parser.HandleState.HANDLED
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `warframe`")
                withDesc("Wrapper for Warframe-related commands.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```warframe [subcommand] [args]```", false)
                appendField("Subcommand: `news`", "Displays the latest Warframe news, same as the news segment in the orbiter.", false)
                withFooterText("Package: ${this@Warframe.javaClass.name}")
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
        }
    }

    private fun updateWorldState() {
        if (!Client.isReady) {
            return
        }

        val timer = measureTimeMillis {
            worldState = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(URL(worldStateUrl))
        }

        logger.debug("updateWorldState(): Parse WorldState took ${timer}ms")
    }

    val updateWorldStateTask = timer("Update WorldState Timer", true, 0, 30000) { updateWorldState() }

    private const val worldStateUrl = "http://content.warframe.com/dynamic/worldState.php"

    var worldState = WorldState()
        private set
}