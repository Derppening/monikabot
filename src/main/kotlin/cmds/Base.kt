package cmds

import Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

/**
 * Interface for implementing bot commands.
 */
interface Base {
    /**
     * Handles [event] for all users.
     *
     * @return Whether the action is handled.
     */
    fun handler(event: MessageReceivedEvent): Parser.HandleState

    /**
     * Handles [event] for superusers.
     *
     * @return Whether the action is handled.
     */
    fun handlerSu(event: MessageReceivedEvent): Parser.HandleState

    /**
     * Shows help text for the command.
     *
     * @param event: The event leading to the invocation of the this function.
     * @param isSu: Whether user invoking this function is a superuser.
     */
    fun help(event: MessageReceivedEvent, isSu: Boolean)
}