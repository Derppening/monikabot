package cmds.warframe

import cmds.IBase
import cmds.Warframe
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.IChannelLogger
import core.Parser
import insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.*

object Cetus : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).toMutableList().apply {
            removeIf { it.matches(Regex("cetus")) }
        }

        when {
            args.any { it.matches(Regex("-{0,2}help")) } -> help(event, false)
            args[0] == "time" -> getTime(event)
            else -> {
                buildMessage(event.channel) {
                    withContent("Stay tuned for more features!")
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `warframe-cetus`")
                withDesc("Displays Cetus-related information.")
                insertSeparator()
                appendField("Usage", "```warframe cetus [time]```", false)
                appendField("`timer`", "If appended, show the current time in Cetus/Plains.", false)
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

    private fun getTime(event: MessageReceivedEvent) {
        val cetusCycleStart = Warframe.worldState.syndicateMissions.find { it.tag == "CetusSyndicate" }?.activation?.date?.numberLong
                ?: throw Exception("Cannot find Cetus information")
        val cetusCycleEnd = Warframe.worldState.syndicateMissions.find { it.tag == "CetusSyndicate" }?.expiry?.date?.numberLong
                ?: throw Exception("Cannot find Cetus information")

        val cetusTimeLeft = run {
            if (Duration.between(Instant.now(), cetusCycleEnd.minus(50, ChronoUnit.MINUTES)).seconds <= 0) {
                Pair(true, Duration.between(Instant.now(), cetusCycleEnd))
            } else {
                Pair(false, Duration.between(Instant.now(), cetusCycleEnd.minus(50, ChronoUnit.MINUTES)))
            }
        }
        val hour = cetusTimeLeft.second.toHours() % 24
        val minute = cetusTimeLeft.second.toMinutes() % 60
        val second = cetusTimeLeft.second.seconds % 60
        val cetusNextDayTime = dateTimeFormatter.format(cetusCycleEnd)
        val cetusNextNightTime = if (!cetusTimeLeft.first) {
            dateTimeFormatter.format(cetusCycleEnd.minus(50, ChronoUnit.MINUTES))
        } else {
            dateTimeFormatter.format(cetusCycleEnd.plus(100, ChronoUnit.MINUTES))
        }
        val cetusNextStateString = if (!cetusTimeLeft.first) "Day" else "Night"
        val timeString = (if (hour > 0) "${hour}h " else "") +
                (if (minute > 0) "${minute}m " else "") +
                "${second}s"

        val cetusDayCycleTime = Duration.between(cetusCycleStart, cetusCycleEnd)
        val dayLengthString = (if (cetusDayCycleTime.toHours() > 0) "${cetusDayCycleTime.toHours()}h " else "") +
                (if (cetusDayCycleTime.toMinutes() % 60 > 0) "${cetusDayCycleTime.toMinutes() % 60}m " else "") +
                if (cetusDayCycleTime.seconds % 60 > 0) "${cetusDayCycleTime.seconds % 60}s" else ""

        buildEmbed(event.channel) {
            withTitle("Cetus Time")
            appendField("Current Time", "$cetusNextStateString - $timeString remaining", false)
            appendField("Next Day Time", "$cetusNextDayTime UTC", true)
            appendField("Next Night Time", "$cetusNextNightTime UTC", true)
            if (dayLengthString.isNotBlank()) {
                appendField("Day Cycle Length", dayLengthString, false)
            }
            withTimestamp(Instant.now())
        }
    }

    private val dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"))
}