package cmds

import Parser
import com.google.gson.Gson
import com.google.gson.JsonParser
import core.Client
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL
import java.util.*
import kotlin.concurrent.timer

object Warframe : Base {
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
        if (!Client.isReady) { return }

        val contents = URL(worldStateLink).readText()

        val time = gson.run {
            val timeElement = JsonParser().parse(contents).asJsonObject.get("Time")
            gson.fromJson(timeElement, Long::class.java)
        }
        WorldState.lastModified = Date(time * 1000)
        val currentTime = Date()

        Log.modifyPersistent("WorldState Last Modified", WorldState.lastModified.toString())
        Log.modifyPersistent("Last Updated", currentTime.toString(), true)
    }

    val bgTask = timer("Update WorldState", true, period = 60000, action = { getWorldState() })

    private const val worldStateLink = "http://content.warframe.com/dynamic/worldState.php"
    private val gson = Gson()

    object WorldState {
        var lastModified = Date()
    }
}
