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
                Log.minus(javaClass.name,
                        "Command \"${event.message.content}\" not handled", event.author, event.channel)
            }
            HandleState.NOT_FOUND -> {
                Log.minus(javaClass.name,
                        "Command \"${event.message.content}\" not found", event.author, event.channel)
            }
        }
    }

    private fun parseSudo(event: MessageReceivedEvent): HandleState {
        val cmd = getCommand(popLeadingMention(event.message.content))

        return when (cmd) {
            "debug" -> Debug.handlerSu(event)
            "status" -> Status.handlerSu(event)
            "stop" -> Stop.handlerSu(event)
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