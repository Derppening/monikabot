package cmds

import core.Core
import core.Log
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder
import kotlin.system.exitProcess

object Stop : Base {
    override fun handler(event: MessageReceivedEvent) {
        throw Exception("Stop should not be allowed by non-admin")
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        if (!Core.isSudoLocationValid(event)) {
            return false
        }

        if (!Core.getArgumentList(event.message.content).isEmpty() &&
                Core.getArgumentList(event.message.content)[0] == "--help") {
            help(event, true)
            return true
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
