package cmds

import core.BuilderHelper.buildEmbed
import core.Core
import core.IChannelLogger
import core.Parser
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

/**
 * Interface for implementing bot commands.
 */
interface IBase : IChannelLogger {
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
            buildEmbed(event.channel) {
                withTitle("Help Text")
                withDesc("No help text is available for this command.")
                withFooterText("Package: ${this@IBase.javaClass.name}")
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

    /**
     * Handles [event] for all users.
     *
     * @return Whether the action is handled.
     */
    fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.PERMISSION_DENIED
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
                .matches(Regex("-{0,2}help"))
    }
}