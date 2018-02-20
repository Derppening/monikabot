package cmds

import com.google.gson.Gson
import com.google.gson.JsonParser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageBuilder
import java.net.URL
import java.util.*

object WorldState : Base {
    private const val worldStateLink = "http://content.warframe.com/dynamic/worldState.php"
    private val gson = Gson()

    override fun handler(event: MessageReceivedEvent) {
        throw Exception("WorldState should not be allowed by non-admin")
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        val contents = URL(worldStateLink).readText()

        MessageBuilder(event.client).apply {
            val time = gson.run {
                val timeElement = JsonParser().parse(contents).asJsonObject.get("Time")
                gson.fromJson(timeElement, Long::class.java)
            }
            val epochTime = Date(time * 1000)

            withChannel(event.channel)
            withCode("", "Raw Time: $time\nLast Updated: $epochTime")
        }.build()

        return true
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        // not handled
    }
}
