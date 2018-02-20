package cmds

import core.Client
import core.Core
import core.Log
import popFirstWord
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Status : Base {
    override fun handler(event: MessageReceivedEvent) {
        throw Exception("Status should not be allowed by non-admin")
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        if (!Core.isSudoLocationValid(event)) {
            return false
        }

        val list = event.message.content.popFirstWord().split(' ').toMutableList()

        val status = when (list[0]) {
            "--help" -> {
                // TODO: Fill help
                return true
            }
            "--reset" -> {
                Client.resetStatus()
                return true
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
            "--online" -> {
                list.removeAt(0)
                Client.Status.ONLINE
            }
            else -> Client.Status.ONLINE
        }

        val message = list.joinToString(" ").removeQuotes()

        try {
            Client.setStatus(status, message)
            Log.plus("Status is set.")
        } catch (e: Exception) {
            Log.minus("Cannot set status: ${e.message}")
            e.printStackTrace()
        }

        return true
    }
}
