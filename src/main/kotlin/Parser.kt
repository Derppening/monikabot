import cmds.Echo
import cmds.Stop
import core.Client
import core.Log
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Parser {
    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        if (event.author == event.client.fetchUser(getBotAdmin())) {
            if (parseSudo(event)) return
        }

        // TODO("Separate explicit action with admin messages")

        val cmd = popLeadingMention(event.message.content)

        when (cmd) {
            "echo" -> Echo.handler(event)
            else -> {
                Log.minus("Message \"${event.message.content}\" not handled.\n" +
                        "\tFrom ${getDiscordTag(event.author)}\n" +
                        "\tIn \"${getChannelId(event.channel)}\"")
            }
        }
    }

    private fun parseSudo(event: MessageReceivedEvent): Boolean {
        val cmd = popLeadingMention(event.message.content)

        return when (cmd) {
            "status" -> Stop.handlerSudo(event)
            "stop" -> Stop.handlerSudo(event)
            else -> false
        }
    }

    private fun getCommand(message: String): String = message.split(' ')[0]

    private fun popLeadingMention(message: String): String {
        return if (message == Client.ourUser.mention(false)) {
            message.popFirstWord()
        } else {
            message
        }
    }
}