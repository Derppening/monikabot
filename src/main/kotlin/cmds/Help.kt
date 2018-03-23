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

import core.BuilderHelper.buildEmbed
import core.Core
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder

object Help : IBase {
    override fun delegateCommand(event: MessageReceivedEvent): Parser.HandleState {
        buildEmbed(event.channel) {
            withTitle("Help Text for MonikaBot")
            withDesc("MonikaBot is a command-based bot, supporting a wide range of features. Written by " +
                    "Derppening#9062.\nUse `[command] --help` to get help text for any command listed below.")
            appendDesc("\nCommands listed as *experimental* can be accessed by appending `--experimental` to the " +
                    "command itself, but note that these commands are subject to change and may not be stable.")
            apply {
                listFunctions(this)
                if (Core.isEventFromSuperuser(event)) {
                    listSuFunctions(this)
                }
            }

            onDiscordError {
                event.author.orCreatePMChannel.sendMessage(this.data())
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
            appendField("`echo`", "Repeats a string.", false)
            appendField("`help`", "Displays help for primary commands.", false)
            appendField("`random`", "Randomly generates numbers.", false)
            appendField("`rng`", "Computes statistics for drop tables.", false)
            appendField("`version`", "Displays the current version of MonikaBot.", false)
            appendField("`warframe`", "Warframe-related commands.", false)
            appendField("Experimental: `trivia`", "Starts a game of trivia.", false)
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
