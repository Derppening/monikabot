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
    @JsonProperty("current_user")
    val currentUser = User()
    val items = Items()

    class Payload {
        @JsonProperty("statistics_closed")
        val statisticsClosed = Statistics<Stats.Closed>()
        @JsonProperty("statistics_live")
        val statisticsLive = Statistics<Stats.Live>()

        class Statistics<T : Stats> {
            @JsonProperty("90days")
            val stat90 = listOf<T>()
            @JsonProperty("48hours")
            val stat48 = listOf<T>()
        }

        sealed class Stats {
            val datetime = ""
            val volume = 0
            @JsonProperty("min_price")
            val minPrice = 0
            @JsonProperty("max_price")
            val maxPrice = 0
            @JsonProperty("avg_price")
            val avgPrice = 0.0
            val median = 0.0
            @JsonProperty("moving_avg")
            val movingAvg = 0.0
            @JsonProperty("wa_price")
            val waPrice = 0.0
            val id = ""

            class Closed : Stats() {
                @JsonProperty("donch_top")
                val donchTop = 0
                @JsonProperty("donch_bot")
                val donchBot = 0
                @JsonProperty("open_price")
                val openPrice = 0.0
                @JsonProperty("closed_price")
                val closedPrice = 0.0
            }

            class Live : Stats() {
                @JsonProperty("order_type")
                val orderType = ""
            }
        }
    }

    class Include {
        val item = Item()

        class Item {
            val id = ""
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
                val fr = Entry()
                val sv = Entry()
                val de = Entry()
                val ko = Entry()
                val ru = Entry()
                val zh = Entry()
                val ducats = 0
                @JsonProperty("trading_tax")
                val tradingTax = 0
                @JsonProperty("url_name")
                val urlName = ""
                val tags = listOf<String>()
                @JsonProperty("set_root")
                val setRoot = false
                val id = ""
                @JsonProperty("icon_format")
                val iconFormat = ""

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

    class User {
        val role = ""
        @JsonProperty("linked_accounts")
        val linkedAccounts = LinkedAccounts()
        @JsonProperty("patreon_profile")
        val patreonProfile = null as Any?
        @JsonProperty("has_mail")
        val hasMail = false
        val banned = false
        val background = null as Any?
        val avatar = null as Any?
        @JsonProperty("check_code")
        val checkCode = ""
        @JsonProperty("ban_until")
        val banUntil = null as Any?
        @JsonProperty("written_reviews")
        val writtenReviews = 0
        @JsonProperty("ban_reason")
        val banReason = null as Any?
        @JsonProperty("ingame_name")
        val ingameName = ""
        @JsonProperty("unread_messages")
        val unreadMessages = 0
        val anonymous = false
        val id = ""
        val region = ""
        val verification = false
        val platform = ""

        class LinkedAccounts {
            @JsonProperty("steam_profile")
            val steamProfile = false
            @JsonProperty("patreon_profile")
            val patreonProfile = false
            @JsonProperty("xbox_profile")
            val xboxProfile = false
        }
    }

    class Items {
        val en = listOf<MarketManifest>()
    }
}