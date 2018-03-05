package cmds

import core.Core
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Reload : IBase, IChannelLogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        Core.reload()

        log(IChannelLogger.LogLevel.INFO, "Properties have been reloaded.") {
            author { event.author }
        }

        return Parser.HandleState.HANDLED
    }
}