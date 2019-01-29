/*
 *  This file is part of MonikaBot.
 *
 *  Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 *  MonikaBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MonikaBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.derppening.monikabot.impl.warframe

import com.derppening.monikabot.impl.WarframeService.worldState
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.ChronoHelper.dateTimeFormatter
import com.derppening.monikabot.util.helpers.ChronoHelper.formatTimeElement
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.NumericHelper.formatReal
import com.derppening.monikabot.util.helpers.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

object CetusService {
    fun getBountyEmbeds(): List<EmbedObject> {
        val cetusInfo = worldState.syndicateMissions.find { it.tag == "CetusSyndicate" }
            ?: throw Exception("Cannot find Cetus information")
        val timeLeft = Duration.between(Instant.now(), cetusInfo.expiry.date.numberLong)
        val timeLeftString = timeLeft.formatDuration()

        val bounties = cetusInfo.jobs

        return bounties.mapIndexed { i, v ->
            buildEmbed {
                withAuthorName("Cetus Bounties - Tier ${i + 1}")
                withTitle(WorldState.getLanguageFromAsset(v.jobType))

                appendField("Mastery Requirement", v.masteryReq.toString(), false)
                appendField("Enemy Level", "${v.minEnemyLevel}-${v.maxEnemyLevel}", true)
                appendField("Total Standing Reward", v.xpAmounts.sum().toString(), true)

                appendField("Expires in", timeLeftString, false)
                withTimestamp(Instant.now())
            }.build()
        }
    }

    fun getGhoulEmbeds(): List<EmbedObject> {
        val ghoulBounties = try {
            worldState.goals.first { it.tag == "GhoulEmergence" }
        } catch (nsee: NoSuchElementException) {
            return emptyList()
        }

        val healthPct = formatReal(ghoulBounties.healthPct, isPercent = true)
        val ghoulTimeLeftString = Duration.between(Instant.now(), ghoulBounties.expiry.date.numberLong).formatDuration()
        val ghoulDesc = ghoulBounties.desc
        val ghoulTooltip = ghoulBounties.tooltip

        return ghoulBounties.jobs.mapIndexed { i, v ->
            buildEmbed {
                withAuthorName("${WorldState.getLanguageFromAsset(ghoulDesc)} - Tier ${i + 1}")
                withTitle(WorldState.getLanguageFromAsset(v.jobType))
                withDesc(WorldState.getLanguageFromAsset(ghoulTooltip))

                appendField("Mastery Requirement", v.masteryReq.toString(), false)
                appendField("Enemy Level", "${v.minEnemyLevel}-${v.maxEnemyLevel}", true)
                appendField("Total Standing Reward", v.xpAmounts.sum().toString(), true)

                appendField("Current Progress", healthPct, true)
                appendField("Expires in", ghoulTimeLeftString, true)
                withTimestamp(Instant.now())
            }.build()
        }
    }

    fun getPlagueStarEmbeds(): List<EmbedObject> {
        val plaugestar = try {
            worldState.goals.first { it.tag == "InfestedPlains" }
        } catch (nsee: NoSuchElementException) {
            return emptyList()
        }

        val timeLeft = Duration.between(Instant.now(), plaugestar.expiry.date.numberLong).formatDuration()
        val desc = plaugestar.desc
        val tooltip = plaugestar.tooltip

        return plaugestar.jobs.map {
            buildEmbed {
                withAuthorName(WorldState.getLanguageFromAsset(desc))
                withTitle(WorldState.getLanguageFromAsset(it.jobType))
                withDesc(WorldState.getLanguageFromAsset(tooltip))

                appendField("Mastery Requirement", it.masteryReq.toString(), false)
                appendField("Enemy Level", "${it.minEnemyLevel}-${it.maxEnemyLevel}", true)
                appendField("Operational Standing", "${it.xpAmounts.sum()}+", true)

                appendField("Expires in", timeLeft, true)
                withTimestamp(Instant.now())
            }.build()
        }
    }

    fun getTimeEmbed(): EmbedObject {
        val timeContainer = worldState.syndicateMissions.find { it.tag == "CetusSyndicate" }?.let {
            CetusTime(it.activation.date.numberLong, it.expiry.date.numberLong)
        } ?: throw Exception("Cannot find Cetus information")

        return buildEmbed {
            withTitle("Cetus Time")

            timeContainer.timeLeft.also {
                appendField("Current Time", "${it.first} - ${it.second} remaining", false)
            }
//            appendField("Equivalent 24-Hour Time", earthTimeEquivalent, false)
            appendField("Next Day Time", timeContainer.dayTimes.joinToString("\n") { "- $it UTC" }, true)
            appendField("Next Night Time", timeContainer.nightTimes.joinToString("\n") { "- $it UTC" }, true)
            timeContainer.dayLength.also {
                if (it.isNotBlank()) {
                    appendField("Day Cycle Length", it, false)
                }
            }

            withTimestamp(Instant.now())
        }.build()
    }

    private class CetusTime(private val start: Instant, private val end: Instant, private val now: Instant = Instant.now()) {
        companion object {
            private const val NUM_OF_NEXT_TIMES = 3
        }

        private val _timeLeft = run {
            if (Duration.between(now, end.minus(50, ChronoUnit.MINUTES)).seconds <= 0) {
                Pair(TimeState.NIGHT, Duration.between(now, end))
            } else {
                Pair(TimeState.DAY, Duration.between(now, end.minus(50, ChronoUnit.MINUTES)))
            }
        }

        val dayLength = Duration.between(start, end).formatDuration()

        val timeLeft = run {
            val state = _timeLeft.first.toString().toLowerCase().capitalize()
            val timeLeft = _timeLeft.second.formatDuration()

            Pair(state, timeLeft)
        }

        val dayTimes = (0 until NUM_OF_NEXT_TIMES).map {
            dateTimeFormatter.format(end.plus(it * 150L, ChronoUnit.MINUTES))
        }

        val nightTimes = run {
            val nextNight = if (_timeLeft.first != TimeState.NIGHT) {
                end.minus(50, ChronoUnit.MINUTES)
            } else {
                end.plus(100, ChronoUnit.MINUTES)
            }
            (0 until NUM_OF_NEXT_TIMES).map {
                dateTimeFormatter.format(nextNight.plus(it * 150L, ChronoUnit.MINUTES))
            }
        }

        val earthTimeEquiv = run {
            val dayProgression = Duration.between(start, now).seconds.toDouble()
            val dayDuration = Duration.between(start, end).seconds.toDouble()
            val timeTranslation = (1.toDouble() / 6) * dayDuration
            val sec = ((dayProgression + timeTranslation) * 86400 / dayDuration).toInt()
            "${formatTimeElement((sec / 3600) % 24)}:${formatTimeElement(sec / 60 % 60)}:${formatTimeElement(sec % 60)}"
        }

        private enum class TimeState(val start: Duration, val end: Duration) {
            DAWN(Duration.ZERO, Duration.ofMinutes(7).plusSeconds(30)),
            SUNRISE(Duration.ofMinutes(7).plusSeconds(30), Duration.ofMinutes(15)),
            MORNING(Duration.ofMinutes(15), Duration.ofMinutes(30)),
            DAY(Duration.ofMinutes(30), Duration.ofMinutes(70)),
            EVENING(Duration.ofMinutes(70), Duration.ofMinutes(85)),
            SUNSET(Duration.ofMinutes(85), Duration.ofMinutes(92).plusSeconds(30)),
            DUSK(Duration.ofMinutes(92).plusSeconds(30), Duration.ofMinutes(100)),
            NIGHT(Duration.ofMinutes(100), Duration.ofMinutes(150))
        }
    }
}