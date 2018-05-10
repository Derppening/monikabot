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

package com.derppening.monikabot.commands

import com.derppening.monikabot.commands.warframe.*
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.insertSeparator
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Warframe : IBase, ILogger {
    private val commands = mapOf(
            "alert" to Alert,
            "baro" to Baro,
            "cetus" to Cetus,
            "darvo" to Darvo,
            "drop" to Drop,
            "fissure" to Fissure,
            "invasion" to Invasion,
            "news" to News,
            "market" to Market,
            "ping" to ServerPing,
            "prime" to Prime,
            "sale" to Sale,
            "sortie" to Sortie,
            "syndicate" to Syndicate,
            "wiki" to Wiki,

            // aliases
            "alerts" to Alert,
            "drops" to Drop,
            "fissures" to Fissure,
            "invasions" to Invasion,
            "primes" to Prime,
            "sorties" to Sortie,
            "syndicates" to Syndicate,
            "wikia" to Wiki
    )

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
                        content {
                            withContent(":information_source: Assuming you meant warframe-${cmdMatches.entries.first().key}...")
                        }
                    }
                }
                cmdMatches.entries.first().value.delegateCommand(event, args)
            }
            else -> {
                if (cmdMatches.entries.all { it.value == cmdMatches.entries.first().value }) {
                    if (args[0] != cmdMatches.entries.first().key) {
                        buildMessage(event.channel) {
                            content {
                                withContent(":information_source: Assuming you meant warframe-${cmdMatches.entries.first().key}...")
                            }
                        }
                    }
                    cmdMatches.entries.first().value.delegateCommand(event, args)
                } else {
                    buildMessage(event.channel) {
                        content {
                            withContent("Your message matches multiple commands!")
                            appendContent("\n\nYour provided command matches:\n")
                            appendContent(cmdMatches.entries.distinctBy { it.value }.joinToString("\n") {
                                "- warframe ${it.key}"
                            })
                        }
                    }
                }

                Parser.HandleState.HANDLED
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            fields {
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
            }

            onError {
                discordException { e ->
                    log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                        author { event.author }
                        channel { event.channel }
                        info { e.errorMessage }
                    }
                }
            }
        }
    }
}