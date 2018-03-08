package cmds.experimental.warframe

import cmds.IBase
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.Core
import core.IChannelLogger
import core.Parser
import org.jsoup.Jsoup
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object Market : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content).drop(2)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) } ) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        val item = args.toMutableList()
                .apply {
                    removeIf {
                        it =="--experimental" || it == "market"
                    }
                }.joinToString(" ")

        event.channel.toggleTypingStatus()

        var useFallback = false
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

                useFallback = true
            } catch (e: Exception) {
                buildMessage(event.channel) {
                    withContent("Cannot find item $item!")
                }

                return Parser.HandleState.HANDLED
            }
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

            if (!useFallback) {
                appendField("48-Hour Minimum", market.payload.statistics.stat48.last().minPrice.toString(), true)
                appendField("48-Hour Median", market.payload.statistics.stat48.last().median.toString(), true)
                appendField("48-Hour Average", market.payload.statistics.stat48.last().avgPrice.toString(), true)
                appendField("48-Hour Maximum", market.payload.statistics.stat48.last().maxPrice.toString(), true)
                appendField("\u200B", "\u200B", false)
                appendField("90-Day Minimum", market.payload.statistics.stat90.last().minPrice.toString(), true)
                appendField("90-Day Median", market.payload.statistics.stat90.last().median.toString(), true)
                appendField("90-Day Average", market.payload.statistics.stat90.last().avgPrice.toString(), true)
                appendField("90-Day Maximum", market.payload.statistics.stat90.last().maxPrice.toString(), true)
                appendField("\u200B", "\u200B", false)
                appendField("Items in Set", market.include.item.itemsInSet.joinToString("\n") { it.en.itemName }, false)
            }

            if (itemInSet != null) {
                appendDescription(itemInSet.en.codex)
                appendField("\u200B", "\u200B", false)
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
            withTimestamp(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")))
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            BuilderHelper.buildEmbed(event.channel) {
                withTitle("Help Text for `warframe-market` (Experimental)")
                withDesc("Displays market information of any item.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```warframe market [item]```", false)
                appendField("`[item]`", "Item to lookup.", false)
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }

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