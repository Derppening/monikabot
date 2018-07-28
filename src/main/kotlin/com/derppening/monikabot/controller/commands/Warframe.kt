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

package com.derppening.monikabot.controller.commands

import com.derppening.monikabot.controller.CommandInterpreter
import com.derppening.monikabot.controller.commands.warframe.*
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
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

    override fun cmdName(): String = "warframe"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, false)
            return CommandInterpreter.HandleState.HANDLED
        }

        val cmdMatches = commands.filter { it.key.startsWith(args[0]) }
        return when (cmdMatches.size) {
            0 -> {
                help(event, false)
                CommandInterpreter.HandleState.HANDLED
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

                CommandInterpreter.HandleState.HANDLED
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("warframe", event) {
            description { "Wrapper for Warframe-related commands." }
            
            usage("warframe [subcommand] [args]") {
                field("Subcommand: `alerts`") { "Displays ongoing alerts." }
                field("Subcommand: `baro`") { "Displays Baro Ki'Teer information." }
                field("Subcommand: `cetus`") { "Displays Cetus-related information" }
                field("Subcommand: `darvo`") { "Displays ongoing Darvo sale." }
                field("Subcommand: `fissures`") { "Displays ongoing fissure missions." }
                field("Subcommand: `invasion`") { "Displays ongoing invasions, as well as construction status of mini-bosses." }
                field("Subcommand: `news`") { "Displays the latest Warframe news, same as the news segment in the orbiter." }
                field("Subcommand: `market`") { "Displays market information about an item." }
                field("Subcommand: `ping`") { "Displays latency information to the Warframe servers." }
                field("Subcommand: `primes`") { "Displays the most recently released primes, as well as predicts the next few primes." }
                field("Subcommand: `sale`") { "Displays currently onoing item sales." }
                field("Subcommand: `sortie`") { "Displays information about the current sorties." }
                field("Subcommand: `syndicate`") { "Displays missions of a syndicate." }
                field("Subcommand: `wiki`") { "Directly links an item to its Warframe Wikia page." }
            }
        }
    }
}