import cmds.Echo
import cmds.Status
import cmds.Stop
import cmds.WorldState
import core.Client
import core.Log
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Parser {
    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        if (event.author == event.client.fetchUser(getBotAdmin())) {
            if (parseSudo(event)) {
                return
            }
        }

        if (!event.channel.isPrivate &&
                !event.message.content.startsWith(Client.ourUser.mention(false))) {
            return
        }

        // TODO("Separate explicit action with admin messages")

        val cmd = getCommand(popLeadingMention(event.message.content))

        when (cmd) {
            "echo" -> Echo.handler(event)
            else -> {
                Log.minus("Command \"${event.message.content}\" not handled.\n" +
                        "\tFrom ${getDiscordTag(event.author)}\n" +
                        "\tIn \"${getChannelId(event.channel)}\"\n" +
                        "\tReason: Command $cmd not found")
            }
        }
    }

    fun isSudoLocationValid(event: MessageReceivedEvent): Boolean {
        return event.channel == Client.getChannelByID(getDebugChannel()) ||
                event.channel == getAdminPrivateChannel()
    }

    private fun parseSudo(event: MessageReceivedEvent): Boolean {
        val cmd = getCommand(popLeadingMention(event.message.content))

        return when (cmd) {
            "worldstate" -> WorldState.handlerSudo(event)
            "status" -> Status.handlerSudo(event)
            "stop" -> Stop.handlerSudo(event)
            else -> false
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