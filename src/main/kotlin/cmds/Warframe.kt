package cmds

import IConsoleLogger
import Parser
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import core.Client
import core.Core.getMethodName
import core.Log
import org.slf4j.LoggerFactory
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageBuilder
import java.net.URL
import java.util.*
import kotlin.concurrent.timer

object Warframe : Base, IConsoleLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Parser.popLeadingMention(event.message.content).popFirstWord().split("\n")

        return when (args[0]) {
            "news" -> getNews(event)
            else -> Parser.HandleState.UNHANDLED
        }
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        // not handled
    }

    private fun getNews(event: MessageReceivedEvent): Parser.HandleState {
        val messageId = MessageBuilder(event.client).apply {
            withCode("", "Updating world state...")
            withChannel(event.channel)
        }.build().longID

        updateWorldState(getMethodName())

        event.client.getMessageByID(messageId).edit("```\nLoading...```")

        val events = JsonParser().parse(worldStateText).asJsonObject.get("Events")
        val eventList = gson.fromJson(events, JsonArray::class.java).asJsonArray
        var eventStr = ""
        for (wfEvent in eventList) {
            val e = wfEvent.asJsonObject
            val time = gson.fromJson(wfEvent.asJsonObject.get("Date").asJsonObject.get("\$date").asJsonObject.get("\$numberLong"), Long::class.java)
            for (it in e.get("Messages").asJsonArray) {
                if (gson.fromJson(it.asJsonObject.get("LanguageCode"), String::class.java) == "en") {
                    eventStr += "${Date(time)} - ${gson.fromJson(it.asJsonObject.get("Message"), String::class.java)}\n"
                    break
                }
            }
        }

        event.client.getMessageByID(messageId).edit("```\n${eventStr.dropLastWhile { it == '\n' }}```")

        return Parser.HandleState.HANDLED
    }

    private fun updateWorldState(invokedBy: String = "") {
        logger.info("Invoked updateWorldState() ${if (invokedBy.isNotBlank()) "from $invokedBy" else ""}")

        if (!Client.isReady) {
            return
        }

        worldStateText = URL(worldStateLink).readText()

        val time = gson.run {
            val timeElement = JsonParser().parse(worldStateText).asJsonObject.get("Time")
            gson.fromJson(timeElement, Long::class.java)
        }
        WorldState.lastModified = Date(time * 1000)
        val currentTime = Date()

        Log.modifyPersistent("Warframe", "WorldState Last Modified", WorldState.lastModified.toString())
        Log.modifyPersistent("Misc", "Last Updated", currentTime.toString(), true)
    }

    val updateWorldStateTask = timer("Update WorldState Timer", true, period = 60000) { updateWorldState() }

    private const val worldStateLink = "http://content.warframe.com/dynamic/worldState.php"
    private var worldStateText = ""
    private val gson = Gson()

    override val logger = LoggerFactory.getLogger(this::class.java)!!

    object WorldState {
        var lastModified = Date()
    }
}
