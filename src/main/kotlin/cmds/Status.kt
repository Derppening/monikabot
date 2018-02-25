package cmds

import Parser
import core.Client
import core.Core
import core.Log
import popFirstWord
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

object Status : Base {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        throw Exception("Status should not be allowed by non-admin")
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        if (!Core.isSudoLocationValid(event)) {
            return Parser.HandleState.UNHANDLED
        }

        if (!Core.getArgumentList(event.message.content).isEmpty() &&
                Core.getArgumentList(event.message.content)[0] == "--help") {
            help(event, true)
            return Parser.HandleState.HANDLED
        }

        val list = event.message.content.popFirstWord().split(' ').toMutableList()

        val status = when (list[0]) {
            "--reset" -> {
                Client.resetStatus()
                return Parser.HandleState.HANDLED
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
            Log.plus("STATUS: Updated.")
        } catch (e: Exception) {
            Log.minus("STATUS: Cannot set status." +
                    "\tInvoked by ${Core.getDiscordTag(event.author)}\n" +
                    "\tIn \"${Core.getChannelId(event.channel)}\"" +
                    "\tReason: ${e.message}")
            e.printStackTrace()
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withCode("", "Usage: echo [--online|--idle|--dnd|--offline|--reset] [PLAYINGTEXT]\n" +
                        "Sets the status and playing text of the bot.\n\n" +
                        "If PLAYINGTEXT is not specified, none will be set.")
            }.build()
        } catch (e: DiscordException) {
            Log.minus("STATUS: Cannot display help text.\n" +
                    "\tInvoked by ${Core.getDiscordTag(event.author)}\n" +
                    "\tIn \"${Core.getChannelId(event.channel)}\"" +
                    "\t Reason: ${e.errorMessage}")
            e.printStackTrace()
        }
    }
}
