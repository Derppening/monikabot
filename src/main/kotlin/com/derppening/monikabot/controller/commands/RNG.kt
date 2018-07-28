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
import com.derppening.monikabot.impl.RNGService
import com.derppening.monikabot.impl.RNGService.computeRNGStats
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object RNG : IBase, ILogger {
    override fun cmdName(): String = "rng"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content)

        val result = computeRNGStats(args)
        when (result) {
            is RNGService.Result.Failure -> {
                buildMessage(event.channel) {
                    content {
                        withContent(result.message)
                    }
                }
            }
            is RNGService.Result.Success -> {
                buildEmbed(event.channel) {
                    fields {
                        withTitle("Probability Calculations")
                        result.embeds(this)
                    }
                }
            }
        }

        return CommandInterpreter.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("rng", event) {
            description { "Computes distribution statistics for drop tables." }

            usage("rng p=[PROBABILITY] [n=ATTEMPTS] [k=SUCCESS_TRIAL] [r=ROUND]") {
                def("[PROBABILITY]") { "Specifies item drop chance." }
                def("[n=ATTEMPTS]") { "Optional: Specifies number of attempts to get the item." }
                def("[k=SUCCESSFUL_TRIAL]") { "Optional: Specifies the number of trial which you got the item." }
                def("[r=ROUND]") {
                    "Optional: Specifies rounding. You may use dp to signify decimal places and sf to signify " +
                            "significant figures." +
                            "\nDefaults to 3 decimal places."
                }
            }
        }
    }
}