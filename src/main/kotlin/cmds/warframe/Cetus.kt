/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

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

        try {
            when {
                args.any { it.matches(Regex("-{0,2}help")) } -> help(event, false)
                args.isEmpty() -> getBounties(event)
                args[0] == "time" -> getTime(event)
                else -> {
                    help(event, false)
                }
            }
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent("Warframe is currently updating its information. Please be patient!")
            }

            log(IChannelLogger.LogLevel.ERROR, e.message ?: "Unknown Exception")
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

    /**
     * Retrieves and outputs a list of all current bounties.
     */
    private fun getBounties(event: MessageReceivedEvent) {
        val cetusInfo = Warframe.worldState.syndicateMissions.find { it.tag == "CetusSyndicate" }
                ?: throw Exception("Cannot find Cetus information")
        val timeLeft = Duration.between(Instant.now(), cetusInfo.expiry.date.numberLong)
        val timeLeftString = formatTimeDuration(timeLeft)

        val bounties = cetusInfo.jobs

        bounties.forEachIndexed { i, v ->
            buildEmbed(event.channel) {
                withAuthorName("Cetus Bounties - Tier ${i + 1}")
                withTitle(WorldState.getLanguageFromAsset(v.jobType))

                appendField("Mastery Requirement", v.masteryReq.toString(), false)
                appendField("Enemy Level", "${v.minEnemyLevel}-${v.maxEnemyLevel}", true)
                appendField("Total Standing Reward", v.xpAmounts.sum().toString(), true)

                appendField("Expires in", timeLeftString, false)
                withTimestamp(Instant.now())
            }
        }
    }

    /**
     * Outputs the current time in Cetus.
     */
    private fun getTime(event: MessageReceivedEvent) {
        val cetusCycleStart = Warframe.worldState.syndicateMissions.find { it.tag == "CetusSyndicate" }?.activation?.date?.numberLong
                ?: throw Exception("Cannot find Cetus information")
        val cetusCycleEnd = Warframe.worldState.syndicateMissions.find { it.tag == "CetusSyndicate" }?.expiry?.date?.numberLong
                ?: throw Exception("Cannot find Cetus information")

        val cetusTimeLeft = run {
            if (Duration.between(Instant.now(), cetusCycleEnd.minus(50, ChronoUnit.MINUTES)).seconds <= 0) {
                Pair(CetusTimeState.NIGHT, Duration.between(Instant.now(), cetusCycleEnd))
            } else {
                Pair(CetusTimeState.DAY, Duration.between(Instant.now(), cetusCycleEnd.minus(50, ChronoUnit.MINUTES)))
            }
        }
        val cetusCurrentStateString = cetusTimeLeft.first.toString().toLowerCase().capitalize()
        val timeString = formatTimeDuration(cetusTimeLeft.second)

        val cetusNextDayTime = dateTimeFormatter.format(cetusCycleEnd)
        val cetusNextNightTime = if (cetusTimeLeft.first == CetusTimeState.DAY) {
            dateTimeFormatter.format(cetusCycleEnd.minus(50, ChronoUnit.MINUTES))
        } else {
            dateTimeFormatter.format(cetusCycleEnd.plus(100, ChronoUnit.MINUTES))
        }

        val dayLengthString = formatTimeDuration(Duration.between(cetusCycleStart, cetusCycleEnd))

        buildEmbed(event.channel) {
            withTitle("Cetus Time")
            appendField("Current Time", "$cetusCurrentStateString - $timeString remaining", false)
            appendField("Next Day Time", "$cetusNextDayTime UTC", true)
            appendField("Next Night Time", "$cetusNextNightTime UTC", true)
            if (dayLengthString.isNotBlank()) {
                appendField("Day Cycle Length", dayLengthString, false)
            }
            withTimestamp(Instant.now())
        }
    }

    /**
     * Formats a duration.
     */
    private fun formatTimeDuration(duration: Duration): String {
        return (if (duration.toDays() > 0) "${duration.toDays()}d " else "") +
                (if (duration.toHours() % 24 > 0) "${duration.toHours() % 24}h " else "") +
                (if (duration.toMinutes() % 60 > 0) "${duration.toMinutes() % 60}m " else "") +
                "${duration.seconds % 60}s"
    }

    private val dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"))

    private enum class CetusTimeState {
        SUNRISE,
        DAY,
        DUSK,
        NIGHT
    }
}