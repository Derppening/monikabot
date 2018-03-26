package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Issue : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        buildMessage(event.channel) {
            withContent("Have a bug report or feature request you would like to submit? Follow this link:")
            appendContent("\n\nhttps://github.com/Derppening/MonikaBot/issues")
        }


        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `issue`")
            withDesc("Shortcut to submitting a bug report or feature request.")
            insertSeparator()
            appendField("Usage", "```issue```", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }
}