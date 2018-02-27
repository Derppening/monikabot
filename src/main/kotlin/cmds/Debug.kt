package cmds

import Parser
import core.Core
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

/**
 * Singleton handling "debug" commands.
 */
object Debug : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.PERMISSION_DENIED
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content)

        assert(args.size > 1)

        when (args[0]) {
            "persist" -> run {
                if (args.size != 4) { return@run }
                Log.modifyPersistent(args[1], args[2], args[3], true)
            }
            else -> {
                Log.minus(javaClass.name,
                        "Unknown debug option \"${args[0]}\"", event.message, event.author, event.channel)
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withCode("","No help text for debug.")
            }.build()
        } catch (e: DiscordException) {
            Log.minus(javaClass.name,
                    "Cannot display help text",
                    null,
                    event.author,
                    event.channel,
                    e.errorMessage)
            e.printStackTrace()
        }
    }
}
