import cmds.*
import core.Client
import core.Core
import core.Log
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

object Parser {
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
            if (!Core.isSudoLocationValid(event)) {
                return
            }
        }

        val cmd = getCommand(popLeadingMention(event.message.content))

        val retval = when (cmd) {
            "clear" -> Clear.delegateCommand(event)
            "debug" -> Debug.delegateCommand(event)
            "echo" -> Echo.delegateCommand(event)
            "random" -> Random.delegateCommand(event)
            "status" -> Status.delegateCommand(event)
            "stop" -> Stop.delegateCommand(event)
            "warframe" -> Warframe.delegateCommand(event)
            else -> HandleState.NOT_FOUND
        }

        when (retval) {
            HandleState.HANDLED -> {}
            HandleState.UNHANDLED -> {
                Log.minus(javaClass.name,
                        "Command \"$cmd\" not handled", event.message, event.author, event.channel)
            }
            HandleState.NOT_FOUND -> {
                try {
                    MessageBuilder(event.client).apply {
                        withChannel(event.channel)
                        withContent("I don't know how to do that! >.<")
                    }.build()
                } catch (e: DiscordException) {}
                Log.minus(javaClass.name,
                        "Command \"$cmd\" not found", event.message, event.author, event.channel)
            }
            HandleState.PERMISSION_DENIED -> {
                try {
                    MessageBuilder(event.client).apply {
                        withChannel(event.channel)
                        withContent("You're not allow to do this! x(")
                    }.build()
                } catch (e: DiscordException) {}
                Log.minus(javaClass.name,
                        "\"$cmd\" was not invoked by superuser", event.message, event.author, event.channel)
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