package cmds

import core.BuilderHelper.buildEmbed
import core.Client
import core.Core
import core.IChannelLogger
import core.Parser
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
        if (!Core.isEventFromOwner(event)) {
            return Parser.HandleState.PERMISSION_DENIED
        } else if (!Core.isOwnerLocationValid(event)) {
            return Parser.HandleState.UNHANDLED
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
                buildEmbed(event.channel) {
                    withTitle("Help Text for `stop`")
                    withDesc("Stops the execution of the bot.")
                    appendField("\u200B", "\u200B", false)
                    appendField("Usage", "```stop```", false)
                    withFooterText("Package: ${this@Stop.javaClass.name}")
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
