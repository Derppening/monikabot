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
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.RandomService.generateRandom
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Random : IBase, ILogger {
    override fun cmdName(): String = "random"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content).toMutableList()

        buildMessage(event.channel) {
            content {
                withContent(generateRandom(args))
            }
        }

        return CommandInterpreter.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("random", event) {
            description { "Randomly generates numbers. Also works for dices and coins." }

            usage("[real] [MIN] [MAX]") {
                flag("real") { "If specified, generate a real number instead of an integer." }
                option("MIN") { "Specify the minimum number (inclusive) to generate." }
                option("MAX") { "Specify the maximum number (inclusive) to generate." }
            }
            usage("list [ENTRIES]...") {
                desc { "Picks one item from a given list." }

                option("ENTRIES") { "A list of entries to pick from, delimited by space." }
            }
            usage("[coin|dice]") {
                desc { "Special modes for the randomizer." }

                flag("coin") { "Flips a coin." }
                flag("dice") { "Rolls a die." }
            }
        }
    }
}