package cmds

import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import kotlin.system.exitProcess

object Stop : Base {
    override fun handler(event: MessageReceivedEvent) {
        throw Exception("Stop should not be allowed by non-admin")
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        event.client.shards.forEach {
            Log.minus("Logging out shard[${it.info[0]}] (Total: ${it.info[1]})")
            it.logout()
        }
        exitProcess(0)
    }
}
