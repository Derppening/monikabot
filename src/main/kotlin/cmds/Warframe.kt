package cmds

import com.google.gson.Gson
import com.google.gson.JsonParser
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL
import java.util.*
import kotlin.concurrent.schedule

object Warframe : Base {
    override fun handler(event: MessageReceivedEvent) {
//        if ()
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        return false
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        // not handled
    }

    private fun getWorldState() {
        val contents = URL(worldStateLink).readText()

        val time = gson.run {
            val timeElement = JsonParser().parse(contents).asJsonObject.get("Time")
            gson.fromJson(timeElement, Long::class.java)
        }
        val epochTime = Date(time * 1000)
        val currentTime = Date()

        Log.modifyPersistent("WorldState Last Modified", epochTime.toString())
        Log.modifyPersistent("WorldState Last Checked", currentTime.toString(), true)
    }

    private val bgTask = Timer().schedule(delay = 30000, period = 300000, action = { getWorldState() })

    private const val worldStateLink = "http://content.warframe.com/dynamic/worldState.php"
    private val gson = Gson()
}
