package cmds

import LoggerInterface
import Parser
import com.google.gson.Gson
import com.google.gson.JsonParser
import core.Client
import core.Log
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL
import java.util.*
import kotlin.concurrent.timer

object Warframe : Base, LoggerInterface {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun handlerSudo(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        // not handled
    }

    private fun getWorldState() {
        logger.info("Invoked getWorldState()")

        if (!Client.isReady) { return }

        val contents = URL(worldStateLink).readText()

        val time = gson.run {
            val timeElement = JsonParser().parse(contents).asJsonObject.get("Time")
            gson.fromJson(timeElement, Long::class.java)
        }
        WorldState.lastModified = Date(time * 1000)
        val currentTime = Date()

        Log.modifyPersistent("Warframe", "WorldState Last Modified", WorldState.lastModified.toString())
        Log.modifyPersistent("Misc", "Last Updated", currentTime.toString(), true)
    }

    val updateWorldStateTask = timer("Update WorldState Timer", true, period = 60000) { getWorldState() }

    private const val worldStateLink = "http://content.warframe.com/dynamic/worldState.php"
    private val gson = Gson()
    override val logger = LoggerFactory.getLogger(this::class.java)!!

    object WorldState {
        var lastModified = Date()
    }
}
