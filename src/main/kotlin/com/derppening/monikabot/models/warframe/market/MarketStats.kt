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

package com.derppening.monikabot.models.warframe.market

import com.fasterxml.jackson.annotation.JsonProperty

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