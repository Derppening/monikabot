package cmds

import Parser
import core.Client
import core.Log
import getBotAdmin
import getChannelId
import getDiscordTag
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

//    private fun adminMessage(event: MessageReceivedEvent) {
//        when (event.message.content.takeWhile { it != ' ' }) {
//            "kill" -> {
//                val message = event.message.content.popFirstWord()
//
//                MessageBuilder(event.client).apply {
//                    withChannel(event.channel)
//                    withCode("py", "print(\"$message\")")
//                }.build()
//            }
//            "status" -> adminChangeStatus(event)
//        }
//    }

object Echo : Base {
    override fun handler(event: MessageReceivedEvent) {
        if (event.message.author == Client.fetchUser(getBotAdmin())) {
            handlerSudo(event)
        }

        val channel = event.channel
        val message = Parser.popLeadingMention(event.message.content).popFirstWord()

        if (!event.channel.isPrivate) {
            try {
                event.message.delete()
            } catch (e: DiscordException) {
                Log.minus("ECHO: Cannot delete \"$message\".\n" +
                        "\tFrom ${getDiscordTag(event.author)}\n" +
                        "\tIn \"${getChannelId(event.channel)}\"\n" +
                        "\tReason: ${e.errorMessage}")
                e.printStackTrace()
            }
        }

        try {
            MessageBuilder(event.client).apply {
                withChannel(channel)
                withContent(message)
            }.build()
        } catch (e: DiscordException) {
            Log.minus("ECHO: \"$message\" not handled.\n" +
                    "\tFrom ${getDiscordTag(event.author)}\n" +
                    "\tIn \"${getChannelId(event.channel)}\"" +
                    "\t Reason: ${e.errorMessage}")
            e.printStackTrace()
        }
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        return false
    }
}
