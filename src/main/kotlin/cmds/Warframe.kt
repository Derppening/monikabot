package cmds

import Parser
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import core.BuilderHelper.buildEmbed
import core.Client
import core.IChannelLogger
import core.IConsoleLogger
import core.PersistentMessage
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.concurrent.timer
import kotlin.system.measureTimeMillis

/**
 * Singleton handling "warframe" commands
 */
object Warframe : IBase, IChannelLogger, IConsoleLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Parser.popLeadingMention(event.message.content).popFirstWord().split("\n")

        return when (args[0]) {
            "news" -> getAllNews(event)
            else -> Parser.HandleState.UNHANDLED
        }
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
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

    /**
     * Handles "warframe news" subcommand.
     *
     * @param event Event which invoked this function.
     *
     * @return Parser.HandleState.HANDLED
     */
    private fun getAllNews(event: MessageReceivedEvent): Parser.HandleState {
        // TODO(Derppening): Split this into two functions: One for all news, one for

        // potentially long operation. toggle typing to show that the bot is loading.
        event.channel.toggleTypingStatus()

        buildEmbed(event.channel) {
            withTitle("Warframe News")

            val timer = measureTimeMillis {
                val events = JsonParser().parse(WorldState.json).asJsonObject.get("Events")
                val eventList = gson.fromJson(events, JsonArray::class.java).asJsonArray

                for (wfEvent in eventList) {
                    val eventJson = wfEvent.asJsonObject
                    val time = gson.fromJson(wfEvent.asJsonObject.get("Date").asJsonObject.get("\$date").asJsonObject.get("\$numberLong"), Long::class.java)

                    for (it in eventJson.get("Messages").asJsonArray) {
                        if (gson.fromJson(it.asJsonObject.get("LanguageCode"), String::class.java) == "en") {
                            val rfctime = DateTimeFormatter.RFC_1123_DATE_TIME
                                    .withZone(ZoneId.of("UTC"))
                                    .format(Instant.ofEpochMilli(time))

                            appendField(gson.fromJson(it.asJsonObject.get("Message"), String::class.java), rfctime, false)
                            break
                        }
                    }
                }
            }

            logger.debug("getAllNews(): JSON parsing took ${timer}ms.")

            withTimestamp(LocalDateTime.ofInstant(WorldState.lastModified, ZoneId.of("UTC")))
        }

        return Parser.HandleState.HANDLED
    }

    /**
     * Updates the world state json. Will be invoked periodically by updateWorldStateTask.
     */
    private fun updateWorldState() {
        if (!Client.isReady) {
            return
        }

        val timer = measureTimeMillis {
            WorldState.json = URL(WorldState.source).readText()
        }
        logger.debug("updateWorldState(): JSON update took ${timer}ms.")

        val time = gson.run {
            val timeElement = JsonParser().parse(WorldState.json).asJsonObject.get("Time")
            gson.fromJson(timeElement, Long::class.java)
        }
        WorldState.lastModified = Instant.ofEpochSecond(time)

        PersistentMessage.modify("Warframe", "WorldState Last Modified", WorldState.lastModified.toString(), true)
    }

    /**
     * Task for updating world state JSON periodically.
     */
    val updateWorldStateTask = timer("Update WorldState Timer", true, 0, 30000) { updateWorldState() }

    /**
     * JSON parser.
     */
    private val gson = Gson()

    /**
     * Singleton for storing world state JSON.
     */
    private object WorldState {
        /**
         * URL to world state.
         */
        const val source = "http://content.warframe.com/dynamic/worldState.php"

        /**
         * When the JSON is last modified server-side.
         */
        var lastModified = Instant.EPOCH!!
        /**
         * JSON string.
         */
        var json = URL(source).readText().also { lastModified = Instant.now() }
    }
}
