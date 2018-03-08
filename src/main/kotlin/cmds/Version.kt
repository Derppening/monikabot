package cmds

import core.BuilderHelper
import core.BuilderHelper.buildMessage
import core.Core
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Version : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        buildMessage(event.channel) {
            withCode("", "MonikaBot v${Core.monikaVersion}")
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            BuilderHelper.buildEmbed(event.channel) {
                withTitle("Help Text for `version`")
                withDesc("Displays the version information.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```version```", false)
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