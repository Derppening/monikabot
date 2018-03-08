package cmds

import core.BuilderHelper.buildEmbed
import core.Client
import core.Core
import core.IChannelLogger
import core.Parser
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

/**
 * Singleton handling "status" commands
 */
object Status : IBase {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        if (!Core.isEventFromOwner(event)) {
            return Parser.HandleState.PERMISSION_DENIED
        } else if (!Core.isOwnerLocationValid(event)) {
            return Parser.HandleState.UNHANDLED
        }

        val args = Core.getArgumentList(event.message.content).toMutableList()

        val status = when (args[0]) {
            "--reset" -> {
                Client.resetStatus()
                return Parser.HandleState.HANDLED
            }
            "--idle" -> {
                args.removeAt(0)
                Client.Status.IDLE
            }
            "--dnd", "--busy" -> {
                args.removeAt(0)
                Client.Status.BUSY
            }
            "--offline", "--invisible" -> {
                args.removeAt(0)
                Client.Status.OFFLINE
            }
            "--online" -> {
                args.removeAt(0)
                Client.Status.ONLINE
            }
            else -> Client.Status.ONLINE
        }

        val message = args.joinToString(" ").removeQuotes()

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
            buildEmbed(event.channel) {
                withTitle("Help Text for `status`")
                withDesc("Sets the status and playing text of the bot.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```status [--online|--idle|--dnd|--offline] [TEXT]```", false)
                appendField("`--online|--idle|--dnd|--offline`", "New status of the bot", false)
                appendField("`[TEXT]`", "New \"Playing\" message of the bot.", false)
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```status [--reset]```", false)
                appendField("`--reset`", "Resets the status to the default.", false)
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
