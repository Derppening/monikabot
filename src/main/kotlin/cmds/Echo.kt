package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.IChannelLogger
import core.Parser
import insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

/**
 * Singleton handling "echo" commands.
 */
object Echo : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)
        if (args.isEmpty()) {
            help(event, false)
            return Parser.HandleState.HANDLED
        }

        val channel = event.channel
        val message = getArgumentList(event.message.content)

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
                withContent(message.joinToString(" "))
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

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `echo`")
                withDesc("Echo: Repeats a string, and erases it from the current channel.")
                insertSeparator()
                appendField("Usage", "```echo [string]```", false)
                appendField("`[string]`", "String to repeat.", false)
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
