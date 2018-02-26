package cmds

import Parser
import core.Core
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder
import kotlin.system.exitProcess

/**
 * Singleton handling "stop" command.
 */
object Stop : Base {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        throw Exception("Stop should not be allowed by non-admin")
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        if (!Core.isSudoLocationValid(event)) {
            return Parser.HandleState.UNHANDLED
        }

        if (!Core.getArgumentList(event.message.content).isEmpty() &&
                Core.getArgumentList(event.message.content)[0] == "--help") {
            help(event, true)
            return Parser.HandleState.HANDLED
        }

        Log.minus(javaClass.name, "Logging out", null, event.author, event.channel)

        event.client.shards.forEach {
            Log.minus(javaClass.name, "Logging out shard[${it.info[0]}] (Total: ${it.info[1]})")
            it.logout()
        }
        exitProcess(0)
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        if (isSu) {
            try {
                MessageBuilder(event.client).apply {
                    withChannel(event.channel)
                    withCode("", "Usage: stop\n" +
                            "Stops the execution of the bot.")
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
}
