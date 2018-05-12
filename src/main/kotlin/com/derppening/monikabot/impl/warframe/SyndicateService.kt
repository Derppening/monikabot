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

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.WarframeService.worldState
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.time.Duration
import java.time.Instant

object SyndicateService : ILogger {
    fun findSyndicateFromTag(tag: String): List<WorldState.SyndicateMission> {
        return worldState.syndicateMissions.filter {
            WorldState.getSyndicateName(it.tag).contains(tag, true)
        }
    }

    fun WorldState.SyndicateMission.toEmbed(): EmbedObject {
        return buildEmbed {
            val name = WorldState.getSyndicateName(tag)
            val timeToExpiry = Duration.between(Instant.now(), expiry.date.numberLong).formatDuration()

            withTitle(name)
            appendField("Expires in", timeToExpiry, false)
            if (nodes.isNotEmpty()) {
                appendField("Nodes", nodes.joinToString("\n") { "- ${WorldState.getSolNode(it).value}" }, false)
            }

            withTimestamp(Instant.now())
        }.build()
    }
}