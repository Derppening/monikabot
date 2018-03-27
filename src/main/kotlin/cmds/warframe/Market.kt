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
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import org.jsoup.Jsoup
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Instant

object Market : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        val item = args.joinToString(" ")

        if (item.isBlank()) {
            buildMessage(event.channel) {
                withContent("Please specify an item to lookup!")
            }

            return Parser.HandleState.HANDLED
        }

        event.channel.toggleTypingStatus()

        val (useFallback, market) = getMarketJson(item).let {
            if (it.first == 2) {
                buildMessage(event.channel) {
                    withContent("Cannot find item $item!")
                }

                return Parser.HandleState.HANDLED
            }
            Pair(it.first == 1, it.second)
        }

        val link = if (useFallback) {
            "https://warframe.market/items/${item.replace(' ', '_').toLowerCase()}_set/statistics"
        } else {
            "https://warframe.market/items/${item.replace(' ', '_').toLowerCase()}/statistics"
        }

        val itemInSet = market.include.item.itemsInSet.find {
            it.urlName == item.replace(' ', '_').toLowerCase() ||
                    it.urlName == (item.replace(' ', '_').toLowerCase() + "_set")
        }

        buildEmbed(event.channel) {
            if (useFallback) {
                withTitle("Item Info")
            } else {
                withTitle("Trade Statistics")
            }

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
                appendDescription(itemInSet.en.codex)
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
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-market`")
            withDesc("Displays market information of any item.")
            insertSeparator()
            appendField("Usage", "```warframe market [item]```", false)
            appendField("`[item]`", "Item to lookup.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    /**
     * Gets and parses the market JSON.
     *
     * @param item Item to retrieve.
     *
     * @return Pair of return code and MarketStats object. If return code is 2, the requested item cannot be found.
     */
    private fun getMarketJson(item: String): Pair<Int, MarketStats> {
        var useFallback = 0
        val link = "https://warframe.market/items/${item.replace(' ', '_').toLowerCase()}/statistics"
        val linkFallback = "https://warframe.market/items/${item.replace(' ', '_').toLowerCase()}_set/statistics"
        val jsonToParse = Jsoup.connect(link)
                .timeout(5000)
                .get()
                .select("#application-state")

        var market: MarketStats
        try {
            market = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(jsonToParse.html())
        } catch (e: Exception) {
            val jsonToParseFallback = Jsoup.connect(linkFallback)
                    .timeout(5000)
                    .get()
                    .select("#application-state")

            try {
                market = jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readValue(jsonToParseFallback.html())

                useFallback = 1
            } catch (e: Exception) {
                return Pair(2, MarketStats())
            }
        }

        return Pair(useFallback, market)
    }

    /**
     * Fixed link for warframe market images.
     */
    private const val imageLink = "https://warframe.market/static/assets/"

    class MarketStats {
        val payload = Payload()
        val include = Include()

        class Payload {
            val statistics = Statistics()

            class Statistics {
                @JsonProperty("90days")
                val stat90 = listOf<Stat>()
                @JsonProperty("48hours")
                val stat48 = listOf<Stat>()
            }

            class Stat {
                @JsonProperty("min_price")
                val minPrice = 0
                val median = 0.0
                @JsonProperty("closed_price")
                val closedPrice = 0.0
                @JsonProperty("moving_avg")
                val movingAvg = 0.0
                @JsonProperty("donch_top")
                val donchTop = 0
                @JsonProperty("max_price")
                val maxPrice = 0
                val datetime = ""
                @JsonProperty("donch_bot")
                val donchBot = 0
                val volume = 0
                @JsonProperty("open_price")
                val openPrice = 0.0
                @JsonProperty("avg_price")
                val avgPrice = 0.0
            }
        }

        class Include {
            val item = Item()

            class Item {
                @JsonProperty("items_in_set")
                val itemsInSet = listOf<ItemInSet>()

                class ItemInSet {
                    @JsonProperty("sub_icon")
                    val subIcon = ""
                    val thumb = ""
                    val icon = ""
                    @JsonProperty("mastery_level")
                    val masteryLevel = 0
                    val en = Entry()
                    val ducats = 0
                    @JsonProperty("trading_tax")
                    val tradingTax = 0
                    @JsonProperty("url_name")
                    val urlName = ""

                    class Entry {
                        val codex = ""
                        val drop = listOf<Drop>()
                        @JsonProperty("item_name")
                        val itemName = ""
                        val description = ""
                        @JsonProperty("wiki_link")
                        val wikiLink = ""

                        class Drop {
                            val link = ""
                            val name = ""
                        }
                    }
                }
            }
        }
    }
}