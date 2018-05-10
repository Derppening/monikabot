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
import com.derppening.monikabot.models.warframe.market.MarketManifest
import com.derppening.monikabot.models.warframe.market.MarketStats
import com.derppening.monikabot.util.FuzzyMatcher
import com.derppening.monikabot.util.helpers.EmbedHelper.insertSeparator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.Jsoup
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.time.Instant

object MarketService : ILogger {
    fun getMarketItem(args: List<String>): Result {
        val manifestEntry = findItemInMarket(args).let {
            if (it.first.isNotBlank()) {
                return Result.Failure(it.first)
            } else {
                it.second
            }
        }

        val market = getMarketJson(manifestEntry.urlName).let {
            if (it.first.isNotBlank()) {
                return Result.Failure(it.first)
            } else {
                it.second
            }
        }

        val link = "https://warframe.market/items/${manifestEntry.urlName}/statistics"

        val itemInSet = market.include.item.itemsInSet.find {
            it.urlName == manifestEntry.urlName.toLowerCase()
        }

        return EmbedBuilder().apply {
            withTitle("Trade Statistics")

            if (market.payload.statistics.stat48.isNotEmpty()) {
                appendField("48-Hour Minimum", market.payload.statistics.stat48.last().minPrice.toString(), true)
                appendField("48-Hour Median", market.payload.statistics.stat48.last().median.toString(), true)
                appendField("48-Hour Average", market.payload.statistics.stat48.last().avgPrice.toString(), true)
                appendField("48-Hour Maximum", market.payload.statistics.stat48.last().maxPrice.toString(), true)
                insertSeparator()
            }

            if (market.payload.statistics.stat90.isNotEmpty()) {
                appendField("90-Day Minimum", market.payload.statistics.stat90.last().minPrice.toString(), true)
                appendField("90-Day Median", market.payload.statistics.stat90.last().median.toString(), true)
                appendField("90-Day Average", market.payload.statistics.stat90.last().avgPrice.toString(), true)
                appendField("90-Day Maximum", market.payload.statistics.stat90.last().maxPrice.toString(), true)
                insertSeparator()
            }

            if (market.include.item.itemsInSet.size > 1) {
                appendField("Items in Set", market.include.item.itemsInSet.joinToString("\n") { it.en.itemName }, false)
            }

            if (itemInSet != null) {
                itemInSet.en.codex.also {
                    if (it.length <= 2048) {
                        appendDescription(it)
                    }
                }
                insertSeparator()
                appendField("Item Mastery Rank", itemInSet.masteryLevel.toString(), false)
                if (itemInSet.ducats == 0) {
                    appendField("Ducats", "(Cannot be traded into Ducats)", true)
                } else {
                    appendField("Ducats", itemInSet.ducats.toString(), true)
                }
                appendField("Trading Tax", itemInSet.tradingTax.toString(), true)
                if (itemInSet.en.drop.isNotEmpty()) {
                    appendField("Drop Locations", itemInSet.en.drop.joinToString("\n") { it.name }, false)
                } else {
                    appendField("Drop Locations", "(None)", false)
                }

                withAuthorName(itemInSet.en.itemName)
                withAuthorUrl(itemInSet.en.wikiLink)
                withAuthorIcon(imageLink + itemInSet.thumb)
                withImage(imageLink + itemInSet.subIcon)
            }

            withUrl(link)
            withTimestamp(Instant.now())
        }.build().let { Result.Success(it) }
    }

    /**
     * Finds an item from the Warframe Market manifest.
     *
     * @param search Search string.
     * @param event Event which invoked the "warframe market" command.
     *
     * @return Manifest of the item.
     */
    private fun findItemInMarket(search: List<String>): Pair<String, MarketManifest> {
        val link = "https://warframe.market/"
        val jsonToParse = Jsoup.connect(link)
                .timeout(5000)
                .get()
                .select("#application-state")

        val manifest = jsonMapper.readTree(jsonToParse.html())
                .get("items").get("en").let {
                    jsonMapper.readValue<List<MarketManifest>>(it.toString())
                }.sortedBy { it.itemName }

        FuzzyMatcher(search, manifest.map { it.itemName }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.isEmpty()) {
                return "Cannot find item with given search!" to MarketManifest()
            } else if (it.size > 1) {
                return "Multiple results match your given search! Including:" +
                        "\n\n${it.take(5).joinToString("\n")}" +
                        "\n\n... With a total of ${it.size} results." to MarketManifest()
            }
        }.first().let { match ->
            return "" to manifest.first { it.itemName == match }
        }
    }

    /**
     * Gets and parses the market JSON.
     *
     * @param item Item to retrieve.
     *
     * @return Pair of return code and MarketStats object. If returns false, the requested item cannot be found.
     */
    private fun getMarketJson(item: String): Pair<String, MarketStats> {
        val link = "https://warframe.market/items/$item/statistics"
        val jsonToParse = Jsoup.connect(link)
                .timeout(5000)
                .get()
                .select("#application-state")

        val market = try {
            jsonMapper.readValue<MarketStats>(jsonToParse.html())
        } catch (e: Exception) {
            return Pair("Cannot find item!", MarketStats())
        }

        return Pair("", market)
    }

    sealed class Result {
        class Success(val embed: EmbedObject) : Result()
        class Failure(val message: String) : Result()
    }

    /**
     * Fixed link for warframe market images.
     */
    private const val imageLink = "https://warframe.market/static/assets/"

    private val jsonMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }
}