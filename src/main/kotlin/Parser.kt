import cmds.*
import core.Client
import core.Core
import core.Log
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Parser {
    enum class HandleState {
        HANDLED,
        UNHANDLED,
        NOT_FOUND
    }

    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        if (Core.isEventFromAdmin(event)) {
            if (parseSudo(event) == HandleState.HANDLED) {
                return
            }
        }

        if (!event.channel.isPrivate &&
                !event.message.content.startsWith(Client.ourUser.mention(false))) {
            return
        }

        val cmd = getCommand(popLeadingMention(event.message.content))

        val retval = when (cmd) {
            "echo" -> Echo.handler(event)
            "warframe" -> Warframe.handler(event)
            else -> HandleState.NOT_FOUND
        }

        when (retval) {
            HandleState.HANDLED -> {}
            HandleState.UNHANDLED -> {
                Log.minus("Command \"${event.message.content}\" not handled.\n" +
                        "\tFrom ${Core.getDiscordTag(event.author)}\n" +
                        "\tIn \"${Core.getChannelId(event.channel)}\"\n" +
                        "\tReason: Command $cmd not handled")
            }
            HandleState.NOT_FOUND -> {
                Log.minus("Command \"${event.message.content}\" not handled.\n" +
                        "\tFrom ${Core.getDiscordTag(event.author)}\n" +
                        "\tIn \"${Core.getChannelId(event.channel)}\"\n" +
                        "\tReason: Command $cmd not found")
            }
        }
    }

    private fun parseSudo(event: MessageReceivedEvent): HandleState {
        val cmd = getCommand(popLeadingMention(event.message.content))

        return when (cmd) {
            "debug" -> Debug.handlerSudo(event)
            "status" -> Status.handlerSudo(event)
            "stop" -> Stop.handlerSudo(event)
            else -> HandleState.NOT_FOUND
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