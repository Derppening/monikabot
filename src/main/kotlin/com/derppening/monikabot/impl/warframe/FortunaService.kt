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
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.time.Duration
import java.time.Instant

object FortunaService {
    fun getBountyEmbeds(): List<EmbedObject> {
        val fortunaInfo = worldState.syndicateMissions.find { it.tag == "SolarisSyndicate" }
            ?: throw Exception("Cannot find Solaris United information")
        val timeLeft = Duration.between(Instant.now(), fortunaInfo.expiry.date.numberLong)
        val timeLeftString = timeLeft.formatDuration()

        val bounties = fortunaInfo.jobs

        return bounties.mapIndexed { i, v ->
            buildEmbed {
                withAuthorName("Fortuna Bounties - Tier ${i + 1}")
                withTitle(WorldState.getLanguageFromAsset(v.jobType))

                appendField("Mastery Requirement", v.masteryReq.toString(), false)
                appendField("Enemy Level", "${v.minEnemyLevel}-${v.maxEnemyLevel}", true)
                appendField("Total Standing Reward", v.xpAmounts.sum().toString(), true)

                appendField("Expires in", timeLeftString, false)
                withTimestamp(Instant.now())
            }.build()
        }
    }

    fun getTimeEmbed(): EmbedObject {
        val timeContainer = FortunaTimes()

        return buildEmbed {
            withTitle("Fortuna Time")

            timeContainer.timeLeft.also {
                appendField("Current Time", "${it.first} - ${it.second} remaining", false)
            }
            appendField("Next Warm Time", timeContainer.warmTimes.joinToString("\n") { "- $it UTC" }, true)

            withTimestamp(Instant.now())
        }.build()
    }

    private class FortunaTimes(private val now: Instant = Instant.now()) {
        companion object {
            private const val NUM_OF_NEXT_TIMES = 3
            private val absStart = Instant.ofEpochSecond(1542056840)
            private val cycleDuration = Duration.ofSeconds(1600)
        }

        private val _timeLeft = run {
                val currentTime = (now.epochSecond - absStart.epochSecond) % cycleDuration.seconds
                TimeState.values().find {
                    it.start.seconds < currentTime && it.end.seconds > currentTime
                }?.let {
                    Pair(it, currentTime)
                } ?: throw IllegalStateException("Cannot find time descriptor")
            }

        val timeLeft = run {
            val state = _timeLeft.first.name.trim { c -> c.isDigit() }.toLowerCase().capitalize()
            val timeLeft = _timeLeft.let { Duration.ofSeconds(it.first.end.seconds - it.second).formatDuration() }

            Pair(state, timeLeft)
        }

        val warmTimes = run {
            val nextWarm = if (_timeLeft.first == TimeState.COLD1) {
                Instant.now().minusSeconds(_timeLeft.second)
            } else {
                Instant.now().plusSeconds(cycleDuration.seconds - _timeLeft.second)
            }

            (0 until NUM_OF_NEXT_TIMES).map {
                dateTimeFormatter.format(nextWarm.plus(cycleDuration.multipliedBy(it.toLong())))
            }
        }

        private enum class TimeState(val start: Duration, val end: Duration) {
            WARM(Duration.ZERO, Duration.ofSeconds(400)),
            COLD1(Duration.ofSeconds(400), Duration.ofSeconds(800)),
            FREEZING(Duration.ofSeconds(800), Duration.ofSeconds(1266)),
            COLD2(Duration.ofSeconds(1266), Duration.ofSeconds(1600))
        }
    }
}