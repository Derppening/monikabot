package cmds

import core.BuilderHelper
import core.Core
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Reload : IBase, IChannelLogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        Core.reload()
        Parser.loadNullResponses()

        log(IChannelLogger.LogLevel.INFO, "Properties have been reloaded.") {
            author { event.author }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            BuilderHelper.buildEmbed(event.channel) {
                withTitle("Help Text for `reload`")
                withDesc("Reloads essential bot properties from their respective files.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```reload```", false)
                withFooterText("Package: ${this@Reload.javaClass.name}")
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