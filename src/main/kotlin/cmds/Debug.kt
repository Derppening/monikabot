package cmds

import Parser
import core.Core
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Debug : Base {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        throw Exception("Debug should not be allowed by non-admin")
    }

    override fun handlerSudo(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content)

        assert(args.size > 1)

        when (args[0]) {
            "persist" -> run {
                if (args.size != 3) { return@run }
                Log.modifyPersistent(args[1], args[2], true)
            }
            else -> {}
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        // nope
    }
}
