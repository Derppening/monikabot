package cmds

import core.BuilderHelper.buildEmbed
import core.Core
import core.Core.getChannelName
import core.Core.getDiscordTag
import core.IChannelLogger
import core.Parser
import core.PersistentMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.awt.Color
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Singleton handling "debug" commands.
 */
object Debug : IBase, IChannelLogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, true)
            return Parser.HandleState.HANDLED
        }

        when (args[0].toLowerCase()) {
            "buildembed" -> run {
                buildEmbed(event.channel) {
                    withTitle("Title")
                    withDesc("Description")
                    withAuthorName("Author Name")
                    withColor(Color.RED)
                    withTimestamp(LocalDateTime.now(ZoneId.of("UTC")))
                    appendField("Field Key", "Field Content", false)
                    withFooterText("Footer Text")
                }
            }
            "log" -> run {
                buildEmbed(event.channel) {
                    withColor(Color.RED)
                    withTitle("Error")
                    withDesc("Cannot display help text")
                    appendField("\u200B", "\u200B", false)
                    appendField("Caused by", "`${event.message.content}`", false)
                    appendField("From", getDiscordTag(event.author), false)
                    appendField("In", getChannelName(event.channel), false)
                    appendField("Additional Info", ":(", false)
                }
            }
            "persist" -> run {
                if (args.size == 2 && args[1].matches("-{0,2}help".toRegex())) {
                    buildEmbed(event.channel) {
                        withTitle("Help Text for `debug-persist`")
                        withDesc("Directly modifies persistence text.")
                        appendField("\u200B", "\u200B", false)
                        appendField("Usage", "```debug persist [header] [key] [value]```", false)
                        appendField("`[header]`", "Header to put the key/value pair", false)
                        appendField("`[key]`", "Name of the value", false)
                        appendField("`[value]`", "Value", false)
                    }
                }
                if (args.size != 4) { return@run }
                PersistentMessage.modify(args[1], args[2], args[3], true)
            }
            else -> {
                log(IChannelLogger.LogLevel.ERROR, "Unknown debug option \"${args[0]}\"") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `debug`")
                withDesc("Enables superuser debugging methods.")
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
