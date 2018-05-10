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

import com.derppening.monikabot.core.Core.isFromSuperuser
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder

object Help : IBase {
    override fun delegateCommand(event: MessageReceivedEvent, args: List<String>): Parser.HandleState {
        buildEmbed(event.channel) {
            val builder = fields {
                withAuthorName("Github")
                withAuthorUrl("https://github.com/Derppening/MonikaBot")
                withTitle("Help Text for MonikaBot")
                withDesc("MonikaBot is a command-based bot, supporting a wide range of features. Written by " +
                        "Derppening#9062.\nUse `[command] --help` to get help text for any command listed below.")
                appendDesc("\nCommands listed as *experimental* can be accessed by appending `--experimental` to the " +
                        "command itself, but note that these commands are subject to change and may not be stable.")
                apply {
                    listFunctions(this)
                    if (event.isFromSuperuser()) {
                        listSuFunctions(this)
                    }
                }
            }

            onError {
                discordException {
                    sendEmbed(event.author.orCreatePMChannel, builder)
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    /**
     * Displays a list of commands for all users.
     */
    private fun listFunctions(embed: EmbedBuilder): EmbedBuilder {
        return embed.apply {
            appendField("`changelog`", "Views the changelog of MonikaBot.", false)
            appendField("`dog`", "Retrieves a photo of a dog.", false)
            appendField("`echo`", "Repeats a string.", false)
            appendField("`help`", "Displays help for primary commands.", false)
            appendField("`issue`", "Shortcut to submitting a bug report or feature request.", false)
            appendField("`metar`", "Displays METAR information for an airfield.", false)
            appendField("`ping`", "Displays latency information of the bot.", false)
            appendField("`random`", "Randomly generates numbers.", false)
            appendField("`reminder`", "Adds a reminder for yourself.", false)
            appendField("`rng`", "Computes statistics for drop tables.", false)
            appendField("`trivia`", "Starts a game of trivia.", false)
            appendField("`version`", "Displays the current version of MonikaBot.", false)
            appendField("`warframe`", "Warframe-related commands.", false)
        }
    }

    /**
     * Displays a list of commands for superusers.
     */
    private fun listSuFunctions(embed: EmbedBuilder): EmbedBuilder {
        return embed.apply {
            appendField("Superuser: `clear`", "Clears all messages in a channel.", false)
            appendField("Superuser: `config`", "Configures properties of MonikaBot.", false)
            appendField("Superuser: `debug`", "Debugging commands.", false)
            appendField("Superuser: `reload`", "Reload important bot properties.", false)
            appendField("Owner: `status`", "Changes the status of the bot.", false)
            appendField("Owner: `stop`", "Terminates the bot.", false)
        }
    }
}
