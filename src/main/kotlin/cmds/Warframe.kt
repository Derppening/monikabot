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

import cmds.warframe.*
import cmds.warframe.Ping
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.BuilderHelper.toEmbedObject
import core.Client
import core.Core
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL
import java.time.Duration
import java.time.Instant
import kotlin.concurrent.timer
import kotlin.system.measureTimeMillis

object Warframe : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, false)
            return Parser.HandleState.HANDLED
        }

        val cmdMatches = commands.filter { it.key.startsWith(args[0]) }
        return when (cmdMatches.size) {
            0 -> {
                help(event, false)
                Parser.HandleState.HANDLED
            }
            1 -> {
                if (args[0] != cmdMatches.entries.first().key) {
                    buildMessage(event.channel) {
                        withContent(":information_source: Assuming you meant warframe-${cmdMatches.entries.first().key}...")
                    }
                }
                cmdMatches.entries.first().value.delegateCommand(event, args)
            }
            else -> {
                if (cmdMatches.entries.all { it.value == cmdMatches.entries.first().value }) {
                    if (args[0] != cmdMatches.entries.first().key) {
                        buildMessage(event.channel) {
                            withContent(":information_source: Assuming you meant warframe-${cmdMatches.entries.first().key}...")
                        }
                    }
                    cmdMatches.entries.first().value.delegateCommand(event, args)
                } else {
                    buildMessage(event.channel) {
                        withContent("Your message matches multiple commands!")
                        appendContent("\n\nYour provided command matches:\n")
                        appendContent(commands.filter { it.key.startsWith(args[0]) }.entries.distinctBy { it.value }.joinToString("\n") {
                            "- warframe ${it.key}"
                        })
                    }
                }

                Parser.HandleState.HANDLED
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe`")
            withDesc("Wrapper for Warframe-related commands.")
            insertSeparator()
            appendField("Usage", "```warframe [subcommand] [args]```", false)
            appendField("Subcommand: `alerts`", "Displays ongoing alerts.", false)
            appendField("Subcommand: `baro`", "Displays Baro Ki'Teer information.", false)
            appendField("Subcommand: `cetus`", "Displays Cetus-related information", false)
            appendField("Subcommand: `darvo`", "Displays ongoing Darvo sale.", false)
            appendField("Subcommand: `fissures`", "Displays ongoing fissure missions.", false)
            appendField("Subcommand: `invasion`", "Displays ongoing invasions, as well as construction status of mini-bosses.", false)
            appendField("Subcommand: `news`", "Displays the latest Warframe news, same as the news segment in the orbiter.", false)
            appendField("Subcommand: `market`", "Displays market information about an item.", false)
            appendField("Subcommand: `ping`", "Displays latency information to the Warframe servers.", false)
            appendField("Subcommand: `primes`", "Displays the most recently released primes, as well as predicts the next few primes.", false)
            appendField("Subcommand: `sale`", "Displays currently onoing item sales.", false)
            appendField("Subcommand: `sortie`", "Displays information about the current sorties.", false)
            appendField("Subcommand: `syndicate`", "Displays missions of a syndicate.", false)
            appendField("Subcommand: `wiki`", "Directly links an item to its Warframe Wikia page.", false)

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
     * Function to update drop tables.
     */
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

    /**
     * Function to update world state.
     */
    private fun updateWorldState() {
        val timer = measureTimeMillis {
            worldState = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(URL(worldStateUrl))
        }

        logger.debug("updateWorldState(): Parse WorldState took ${timer}ms")
    }

    private fun pingServer() {
        if (Core.monikaVersionBranch == "stable" && pingServerMessage != 0L) {
            val embed = Client.getMessageByID(pingServerMessage).embeds.first().toEmbedObject {
                clearFields()
                cmds.warframe.Ping.pingServer(this)
                withTimestamp(Instant.now())
            }
            Client.getMessageByID(pingServerMessage)?.edit(embed)
        }
    }

    /**
     * Formats a duration.
     */
    internal fun Duration.formatDuration(): String =
            (if (toDays() > 0) "${toDays()}d " else "") +
                    (if (toHours() % 24 > 0) "${toHours() % 24}h " else "") +
                    (if (toMinutes() % 60 > 0) "${toMinutes() % 60}m " else "") +
                    "${seconds % 60}s"

    /**
     * Rounds a duration to the smallest time unit, from Seconds to Days.
     */
    internal fun Duration.toNearestChronoDay(): String =
            when {
                toDays() > 0 -> "${toDays()}d"
                toHours() > 0 -> "${toHours()}h"
                toMinutes() > 0 -> "${toMinutes()}m"
                else -> "${seconds}s"
            }

    /**
     * Rounds a duration to the smallest time unit, from Days to (approximated) Years.
     */
    internal fun Duration.toNearestChronoYear(): String =
            when {
                toDays() > 365 -> "${toDays() / 365} years"
                toDays() > 30 -> "${toDays() / 30} months"
                else -> "${toDays()} days"
            }

    val updateDropTablesTask = timer("Update Drop Table Timer", true, 0, 60000) { updateDropTables() }
    val updateWorldStateTask = timer("Update WorldState Timer", true, 0, 30000) { updateWorldState() }
    val pingServerTask = timer("Warframe Ping Task", true, 0, 30000) { pingServer() }

    private const val dropTableDataUrl = "https://raw.githubusercontent.com/WFCD/warframe-drop-data/gh-pages/data/"
    private const val worldStateUrl = "http://content.warframe.com/dynamic/worldState.php"

    private val commands = mapOf(
            "alert" to Alert,
            "baro" to Baro,
            "cetus" to Cetus,
            "darvo" to Darvo,
            "fissure" to Fissure,
            "invasion" to Invasion,
            "news" to News,
            "market" to Market,
            "ping" to Ping,
            "prime" to Prime,
            "sale" to Sale,
            "sortie" to Sortie,
            "syndicate" to Syndicate,
            "wiki" to Wiki,

            // aliases
            "alerts" to Alert,
            "fissures" to Fissure,
            "invasions" to Invasion,
            "primes" to Prime,
            "sorties" to Sortie,
            "syndicates" to Syndicate,
            "wikia" to Wiki
    )

    private var dropTableInfo = DropTable.Info()
    internal var dropTables = DropTable()
        private set
    internal var worldState = WorldState()
        private set
    val pingServerMessage = run {
        if (Core.monikaVersionBranch == "stable") {
            while (!Client.isReady) {
                Thread.sleep(500)
            }

            try {
                val channel = Core.getChannelByName("warframe_ping", Core.getGuildByName("Deisimi Rollers")!!)
                buildEmbed(channel!!) {
                    withTitle("Warframe Latency Information")
                }?.also {
                    while (Client.getMessageByID(it.longID) == null) {
                        Thread.sleep(500)
                    }
                    logger.info("Warframe Ping Persistence has ID ${it.longID} in ${it.channel.longID}")
                }?.longID ?: 0L
            } catch (e: NullPointerException) {
                0L
            }
        } else {
            0L
        }
    }
}