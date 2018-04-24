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

import com.derppening.monikabot.commands.Warframe
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.ChronoHelper.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.time.Duration
import java.time.Instant

object FissureService : ILogger {
    fun getFissureEmbed(): EmbedObject {
        return Warframe.worldState.activeMissions.toEmbed()
    }

    fun List<WorldState.ActiveMission>.toEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            withTitle("Ongoing Fissures")

            this@toEmbed.sortedBy { it.modifier }.forEach {
                val nodeName = WorldState.getSolNode(it.node).value
                val missionType = WorldState.getMissionType(it.missionType)
                val tier = WorldState.getFissureModifier(it.modifier)
                val durationToExpiry = Duration.between(Instant.now(), it.expiry.date.numberLong).formatDuration()

                appendField("$tier $missionType on $nodeName", "Time Left: $durationToExpiry", false)
            }

                withTimestamp(Instant.now())
        }.build()
    }
}