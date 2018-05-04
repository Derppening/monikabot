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
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Random : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).toMutableList()

        buildMessage(event.channel) {
            withContent(generateRandom(args))
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `random`")
            withDesc("Randomly generates numbers. Also works for dices and coins.")
            insertSeparator()
            appendField("Usage", "```random [real] [min] [max]```", false)
            appendField("`real`", "If specified, generate a real number instead of an integer.", false)
            appendField("`[min] [max]`", "Specify the minimum and maximum numbers (inclusive) to generate.", false)
            insertSeparator()
            appendField("Usage", "```random list [entries]", false)
            appendField("`[entries]`", "A list of entries to pick one from, delimited by space.", false)
            insertSeparator()
            appendField("Usage", "```random [coin|dice]```", false)
            appendField("`[coin|dice]`", "Special modes to generate output based on a coin/dice.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }
}