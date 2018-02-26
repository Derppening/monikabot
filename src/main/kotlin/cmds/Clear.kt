package cmds

import Parser
import core.Log
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

object Clear : Base {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        throw Exception("Clear should not be allowed by non-admin")
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = Parser.popLeadingMention(event.message.content).popFirstWord().split(" ").toMutableList()

        if (args[0].matches("-{0,2}help".toRegex())) {
            Random.help(event, false)
            return Parser.HandleState.HANDLED
        }
        val all = args.contains("all") || args.contains("--all")

        if (event.channel.isPrivate) {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withContent("I can't delete clear messages in private channels!")
            }.build()

            Log.minus(javaClass.name,
                    "Cannot bulk delete messages",
                    null,
                    event.author,
                    event.channel,
                    "In a private channel")
        } else {
            val messages = if (all) event.channel.fullMessageHistory else event.channel.messageHistory
            event.channel.bulkDelete(messages.filterNot { it.longID == Log.persistentMessageId })
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withCode("", "Usage: clear [--all]\n" +
                        "Clear: Clears a channel messages which are younger than 14 days.\n\n" +
                        "--all: Retrieves all messages from the channel, not only ones which are locally cached.\n\n" +
                        "This command does not work in private channels.")
            }.build()
        } catch (e: DiscordException) {
            Log.minus(javaClass.name,
                    "Cannot display help text",
                    null,
                    event.author,
                    event.channel,
                    e.errorMessage)
            e.printStackTrace()
        }
    }
}
