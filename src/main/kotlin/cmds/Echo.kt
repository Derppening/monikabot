package cmds

import Parser
import core.Core
import core.Log
import popFirstWord
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

/**
 * Singleton handling "echo" commands.
 */
object Echo : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        if (!Core.getArgumentList(event.message.content).isEmpty() &&
                Core.getArgumentList(event.message.content)[0] == "--help") {
            help(event, false)
            return Parser.HandleState.UNHANDLED
        }

        val channel = event.channel
        val message = Parser.popLeadingMention(event.message.content).popFirstWord()

        if (!event.channel.isPrivate) {
            try {
                event.message.delete()
            } catch (e: DiscordException) {
                Log.minus(javaClass.name,
                        "Cannot delete \"$message\"",
                        null,
                        event.author,
                        event.channel,
                        e.errorMessage)
                e.printStackTrace()
            }
        }

        try {
            MessageBuilder(event.client).apply {
                withChannel(channel)
                withContent(message.removeQuotes())
            }.build()
        } catch (e: DiscordException) {
            Log.minus(javaClass.name,
                    "\"$message\" not handled",
                    event.message,
                    event.author,
                    event.channel,
                    e.errorMessage)
            e.printStackTrace()
        }

        return Parser.HandleState.HANDLED
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withCode("","Usage: echo [string]\n" +
                        "Echo: Repeats a string.")
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
