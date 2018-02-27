package cmds

import Parser
import core.Core
import core.Log
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

/**
 * Interface for implementing bot commands.
 */
interface IBase {
    /**
     * Delegates [event] to the appropriate function.
     *
     * @return Whether the action is handled.
     */
    fun delegateCommand(event: MessageReceivedEvent): Parser.HandleState {
        if (hasHelpFlag(event.message.content)) {
            help(event, Core.isEventFromSuperuser(event))
            return Parser.HandleState.HANDLED
        }

        if (Core.isEventFromSuperuser(event)) {
            val suHandleStatus = handlerSu(event)
            if (suHandleStatus != Parser.HandleState.UNHANDLED) { return suHandleStatus }
        }

        return handler(event)
    }

    /**
     * Shows help text for the command.
     *
     * @param event: The event leading to the invocation of the this function.
     * @param isSu: Whether user invoking this function is a superuser.
     */
    fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withCode("", "No help text is available for this command.")
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

    /**
     * Handles [event] for all users.
     *
     * @return Whether the action is handled.
     */
    fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    /**
     * Handles [event] for superusers.
     *
     * @return Whether the action is handled.
     */
    fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    /**
     * Check whether argument list contains a "help" flag.
     */
    private fun hasHelpFlag(arg0: String): Boolean {
        return Parser.popLeadingMention(arg0)
                .popFirstWord()
                .split(" ").also { if (it.isEmpty()) return false }[0]
                .matches("-{0,2}help".toRegex())
    }
}