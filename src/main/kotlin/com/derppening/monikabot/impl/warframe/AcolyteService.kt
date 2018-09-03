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

import com.derppening.monikabot.impl.WarframeService
import com.derppening.monikabot.models.warframe.Manifest
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.NumericHelper
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.awt.Color

object AcolyteService {
    fun getAcolytes(): List<WorldState.PersistentEnemies> {
        return WarframeService.worldState.persistentEnemies.filter {
            it.agentType.startsWith("/Lotus/Types/Enemies/Acolytes/")
        }
    }

    fun WorldState.PersistentEnemies.toEmbed(): EmbedObject {
        return buildEmbed {
            val name = WorldState.getLanguageFromAsset(locTag)
            val icon = Manifest.getImageLinkFromAssetLocation(icon)

            withTitle(name)

            appendField("Current Health", NumericHelper.formatReal(healthPercent, isPercent = true), false)
            withImage(icon)

            if (discovered) {
                withColor(Color.GREEN)
            } else {
                withColor(Color.RED)
            }

            withFooterText("${if (discovered) "Discovered" else "Last discovered"} in ${WorldState.getSolNode(lastDiscoveredLocation).value}")
            withTimestamp(lastDiscoveredTime.date.numberLong)
        }.build()
    }
}
