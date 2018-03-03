package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.IChannelLogger
import core.Parser
import core.PersistentMessage
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Clear : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.PERMISSION_DENIED
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = Parser.popLeadingMention(event.message.content).popFirstWord().split(" ").toMutableList()

        if (args[0].matches("-{0,2}help".toRegex())) {
            Random.help(event, false)
            return Parser.HandleState.HANDLED
        }
        val all = args.contains("all") || args.contains("--all")

        if (event.channel.isPrivate) {
            buildMessage(event.channel) {
                withContent("I can't delete clear messages in private channels!")
            }

            log(IChannelLogger.LogLevel.ERROR, "Cannot bulk delete messages") {
                author { event.author }
                channel { event.channel }
                info { "In a private channel" }
            }
        } else {
            val messages = if (all) event.channel.fullMessageHistory else event.channel.messageHistory
            event.channel.bulkDelete(messages.filterNot { it.longID == PersistentMessage.messageId })
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `clear`")
                withDesc("Clears all channel messages that are younger than 14 days.")
                appendDesc("\nThis command does not work in private channels.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```clear [--all]```", false)
                appendField("`--all`", "Retrieves all messages from the channel, not only ones which " +
                        "are locally cached.", false)
                withFooterText("Package: ${this@Clear.javaClass.name}")
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
