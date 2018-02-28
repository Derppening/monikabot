package cmds

import Parser
import core.BuilderHelper.buildMessage
import core.Client
import core.Core
import core.IChannelLogger
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import kotlin.system.exitProcess

/**
 * Singleton handling "stop" command.
 */
object Stop : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.PERMISSION_DENIED
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

        log(IChannelLogger.LogLevel.WARN, "Logging out with ${event.client.shardCount} shard(s) active") {
            author { event.author }
            channel { event.channel}
        }

        Client.clearTimers()

        event.client.shards.forEach {
            it.logout()
        }
        exitProcess(0)
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        if (isSu) {
            try {
                buildMessage(event.channel) {
                    withCode("", "Usage: stop\n" +
                            "Stops the execution of the bot.")
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
    }
}
