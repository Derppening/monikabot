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

package cmds

import cmds.warframe.DropTable
import cmds.warframe.Market
import cmds.warframe.News
import cmds.warframe.WorldState
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.IChannelLogger
import core.IConsoleLogger
import core.Parser
import insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.net.URL
import kotlin.concurrent.timer
import kotlin.system.measureTimeMillis

object Warframe : IBase, IChannelLogger, IConsoleLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, false)
            return Parser.HandleState.HANDLED
        }

        return when (args[0]) {
            "news" -> News.handler(event)
            "market" -> Market.handler(event)
            else -> {
                help(event, false)
                Parser.HandleState.HANDLED
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `warframe`")
                withDesc("Wrapper for Warframe-related commands.")
                insertSeparator()
                appendField("Usage", "```warframe [subcommand] [args]```", false)
                appendField("Subcommand: `news`", "Displays the latest Warframe news, same as the news segment in the orbiter.", false)
                appendField("Subcommand: `market`", "Displays market information about an item.", false)
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
        }
    }

    private fun updateDropTables() {
        val info = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.readValue<DropTable.Info>(URL("$dropTableDataUrl/info.json"))

        if (info.hash == dropTableInfo.hash && info.timestamp == dropTableInfo.timestamp) {
            return
        }

        dropTableInfo = info

        val timer = measureTimeMillis {
            dropTables = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(URL("$dropTableDataUrl/all.json"))
        }

        logger.debug("updateDropTables(): Parsing took ${timer}ms")
    }

    private fun updateWorldState() {
        val timer = measureTimeMillis {
            worldState = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(URL(worldStateUrl))
        }

        logger.debug("updateWorldState(): Parse WorldState took ${timer}ms")
    }

    val updateDropTablesTask = timer("Update Drop Table Timer", true, 0, 60000) { updateDropTables() }
    val updateWorldStateTask = timer("Update WorldState Timer", true, 0, 30000) { updateWorldState() }

    private const val dropTableDataUrl = "https://raw.githubusercontent.com/WFCD/warframe-drop-data/gh-pages/data/"
    private const val worldStateUrl = "http://content.warframe.com/dynamic/worldState.php"

    private var dropTableInfo = DropTable.Info()
        private set
    internal var dropTables = DropTable()
        private set
    internal var worldState = WorldState()
        private set
}