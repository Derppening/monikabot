package cmds.warframe

import cmds.Debug
import cmds.IBase
import cmds.Warframe
import core.BuilderHelper
import core.IChannelLogger
import core.Parser
import insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object News : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) } ) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        BuilderHelper.buildEmbed(event.channel) {
            withTitle("Warframe News")

            val eventPairs = mutableMapOf<Instant, String>()

            for (eventItem in Warframe.worldState.events) {
                eventPairs[eventItem.date.date.numberLong] = eventItem.messages.find { it.languageCode == Locale.ENGLISH }?.message ?: ""
            }

            val sortedPairs = eventPairs.entries.sortedBy { it.key }.reversed()
            sortedPairs.forEach { (k, v) ->
                val diff = Duration.between(k, Instant.now())
                val diffString = when {
                    diff.toDays() > 0 -> "${diff.toDays()}d"
                    diff.toHours() > 0 -> "${diff.toHours()}h"
                    diff.toMinutes() > 0 -> "${diff.toMinutes()}m"
                    else -> "${diff.seconds}s"
                }
                if (v.isNotBlank()) {
                    appendDesc("\n[$diffString] $v")
                }
            }

            withTimestamp(LocalDateTime.ofInstant(Warframe.worldState.time, ZoneId.of("UTC")))
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            BuilderHelper.buildEmbed(event.channel) {
                withTitle("Help Text for `warframe-news` (Experimental)")
                withDesc("Displays the latest Warframe news.")
                insertSeparator()
                appendField("Usage", "```warframe news```", false)
            }
        } catch (e: DiscordException) {
            Debug.log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }
}