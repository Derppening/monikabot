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
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.time.Instant

object SaleService : ILogger {
    fun getSaleEmbed(): EmbedObject {
        return worldState.flashSales.toEmbed()
    }

    fun List<WorldState.FlashSale>.toEmbed(): EmbedObject {
        return buildEmbed {
            withTitle("Sales")

            filterNot {
                it.typeName.contains("PrimeAccess")
            }.sortedBy {
                it.bannerIndex
            }.forEach {
                val itemName = WorldState.getLanguageFromAsset(it.typeName).let { item ->
                    if (item.isBlank()) {
                        it.typeName
                    } else {
                        item
                    }
                }
                val appendStr = when {
                    it.featured && it.popular -> "Featured/Popular: "
                    it.featured -> "Featured: "
                    it.popular -> "Popular: "
                    else -> ""
                }
                val valueStr = when {
                    it.regularOverride != 0 && it.premiumOverride != 0 -> "${it.premiumOverride} Platinum + ${it.regularOverride} Credits"
                    it.premiumOverride != 0 -> "${it.premiumOverride} Platinum"
                    it.regularOverride != 0 -> "${it.regularOverride} Credits"
                    else -> ""
                }

                if (valueStr.isNotBlank()) {
                    appendField("$appendStr$itemName", valueStr, false)
                }
            }

            withTimestamp(Instant.now())
        }.build()
    }
}