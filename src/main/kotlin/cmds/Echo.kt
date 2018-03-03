package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.Core
import core.IChannelLogger
import core.Parser
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
            log(IChannelLogger.LogLevel.ERROR, "\"$message\" not handled") {
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
            buildEmbed(event.channel) {
                withTitle("Help Text for `echo`")
                withDesc("Echo: Repeats a string, and erases it from the current channel.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```echo [string]```", false)
                appendField("`[string]`", "String to repeat.", false)
                withFooterText("Package: ${this@Echo.javaClass.name}")
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
