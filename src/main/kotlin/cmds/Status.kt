package cmds

import core.Client
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Status : Base {
    override fun handler(event: MessageReceivedEvent) {
        throw Exception("Status should not be allowed by non-admin")
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        val list = event.message.content.split(' ').drop(0).toMutableList()

        val status = when (list[0]) {
            "--reset" -> {
                list.clear()
                list.add(0, Client.defaultStatus)
                Client.defaultState
            }
            "--idle" -> {
                list.removeAt(0)
                Client.Status.IDLE
            }
            "--dnd", "--busy" -> {
                list.removeAt(0)
                Client.Status.BUSY
            }
            "--offline", "--invisible" -> {
                list.removeAt(0)
                Client.Status.OFFLINE
            }
            else -> Client.Status.ONLINE
        }

        val message = list.joinToString(" ")

        try {
            Client.setStatus(status, message)
            Log.plus("Status \"$message\" is set")
        } catch (e: DiscordException) {
            Log.minus("Cannot set status: ${e.errorMessage}")
            e.printStackTrace()

            return false
        }

        return true
    }
}
