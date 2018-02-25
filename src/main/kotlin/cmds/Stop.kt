package cmds

import Parser
import core.Core
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder
import kotlin.system.exitProcess

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

        event.client.shards.forEach {
            Log.minus("Logging out shard[${it.info[0]}] (Total: ${it.info[1]})")
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
                Log.minus("STOP: Cannot display help text.\n" +
                        "\tInvoked by ${Core.getDiscordTag(event.author)}\n" +
                        "\tIn \"${Core.getChannelId(event.channel)}\"" +
                        "\t Reason: ${e.errorMessage}")
                e.printStackTrace()
            }
        }
    }
}
