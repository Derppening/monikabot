package cmds

import Parser
import core.BuilderHelper.buildMessage
import core.Core
import core.IChannelLogger
import popFirstWord
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

/**
 * Singleton handling "echo" commands.
 */
object Echo : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        if (!Core.getArgumentList(event.message.content).isEmpty() &&
                Core.getArgumentList(event.message.content)[0] == "--help") {
            help(event, false)
            return Parser.HandleState.UNHANDLED
        }

        val channel = event.channel
        val message = Parser.popLeadingMention(event.message.content).popFirstWord()

        if (!event.channel.isPrivate) {
            try {
                event.message.delete()
            } catch (e: DiscordException) {
                log(IChannelLogger.LogLevel.ERROR, "Cannot delete \"$message\"") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
                e.printStackTrace()
            }
        }

        try {
            buildMessage(channel) {
                withContent(message.removeQuotes())
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR,"\"$message\" not handled") {
                message { event.message }
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }

        return Parser.HandleState.HANDLED
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildMessage(event.channel) {
                withCode("","Usage: echo [string]\n" +
                        "Echo: Repeats a string.")
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }
}
