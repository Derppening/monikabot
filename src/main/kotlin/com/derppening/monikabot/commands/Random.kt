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

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.RandomService.generateRandom
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Random : IBase, ILogger {
    override fun cmdName(): String = "random"

    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).toMutableList()

        buildMessage(event.channel) {
            content {
                withContent(generateRandom(args))
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("random", event) {
            description { "Randomly generates numbers. Also works for dices and coins." }

            usage("random [real] [min] [max]") {
                def("real") { "If specified, generate a real number instead of an integer." }
                def("[min] [max]") { "Specify the minimum and maximum numbers (inclusive) to generate." }
            }
            usage("random list [entries]") {
                def("[entries]") { "A list of entries to pick one from, delimited by space." }
            }
            usage("random [coin|dice]") {
                def("[coin|dice]") { "Special modes to generate output based on a coin/dice." }
            }
        }
    }
}