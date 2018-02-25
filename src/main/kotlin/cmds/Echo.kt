package cmds

import Parser
import core.Core
import core.Log
import popFirstWord
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

object Echo : Base {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        if (Core.isEventFromAdmin(event)) {
            handlerSu(event)
        }

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
                Log.minus(javaClass.name, "Cannot delete \"$message\"", event.author, event.channel, e.errorMessage)
                e.printStackTrace()
            }
        }

        try {
            MessageBuilder(event.client).apply {
                withChannel(channel)
                withContent(message.removeQuotes())
            }.build()
        } catch (e: DiscordException) {
            Log.minus(javaClass.name, "\"$message\" not handled", event.author, event.channel, e.errorMessage)
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
                withCode("","Echo: Repeats a user-defined string.")
            }.build()
        } catch (e: DiscordException) {
            Log.minus(javaClass.name, "Cannot display help text", event.author, event.channel, e.errorMessage)
            e.printStackTrace()
        }
    }
}
