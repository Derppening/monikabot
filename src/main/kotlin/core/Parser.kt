package core

import cmds.*
import core.BuilderHelper.buildMessage
import popFirstWord
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Parser : IChannelLogger {
    enum class HandleState {
        HANDLED,
        UNHANDLED,
        PERMISSION_DENIED,
        NOT_FOUND
    }

    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        if (!event.channel.isPrivate &&
                !event.message.content.startsWith(Client.ourUser.mention(false))) {
            if (!Core.isOwnerLocationValid(event)) {
                return
            }
        }

        val cmd = getCommand(popLeadingMention(event.message.content)).toLowerCase()

        val retval = when (cmd) {
            "clear" -> Clear.delegateCommand(event)
            "debug" -> Debug.delegateCommand(event)
            "echo" -> Echo.delegateCommand(event)
            "help" -> Help.delegateCommand(event)
            "random" -> Random.delegateCommand(event)
            "reload" -> Reload.delegateCommand(event)
            "rng" -> RNG.delegateCommand(event)
            "status" -> Status.delegateCommand(event)
            "stop" -> Stop.delegateCommand(event)
            "version" -> Version.delegateCommand(event)
            "warframe" -> Warframe.delegateCommand(event)
            else -> HandleState.NOT_FOUND
        }

        when (retval) {
            HandleState.HANDLED -> {}
            HandleState.UNHANDLED -> {
                log(IChannelLogger.LogLevel.ERROR, "Command \"$cmd\" not handled") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
            HandleState.NOT_FOUND -> {
                try {
                    buildMessage(event.channel) {
                        withContent("I don't know how to do that! >.<")
                    }
                } catch (e: DiscordException) {}
                log(IChannelLogger.LogLevel.ERROR, "Command \"$cmd\" not found") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
            HandleState.PERMISSION_DENIED -> {
                try {
                    buildMessage(event.channel) {
                        withContent("You're not allow to do this! x(")
                    }
                } catch (e: DiscordException) {}
                log(IChannelLogger.LogLevel.ERROR, "\"$cmd\" was not invoked by superuser") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
        }
    }

    fun popLeadingMention(message: String): String {
        return if (message.startsWith(Client.ourUser.mention(false))) {
            message.popFirstWord()
        } else {
            message
        }
    }

    private fun getCommand(message: String): String = message.split(' ')[0]
}