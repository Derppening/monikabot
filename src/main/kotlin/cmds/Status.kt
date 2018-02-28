package cmds

import Parser
import core.BuilderHelper.buildMessage
import core.Client
import core.Core
import core.IChannelLogger
import popFirstWord
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

/**
 * Singleton handling "status" commands
 */
object Status : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.PERMISSION_DENIED
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
            log(IChannelLogger.LogLevel.INFO, "Successfully updated")
        } catch (e: Exception) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot set status") {
                author { event.author }
                channel { event.channel }
                info { e.message ?: "Unknown Exception" }
            }
            e.printStackTrace()
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildMessage(event.channel) {
                withCode("", "Usage: status [--online|--idle|--dnd|--offline|--reset] [PLAYINGTEXT]\n" +
                        "Sets the status and playing text of the bot.\n\n" +
                        "If PLAYINGTEXT is not specified, none will be set.")
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Unable to display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }
}
