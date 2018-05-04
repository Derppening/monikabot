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
import com.derppening.monikabot.impl.RNGService
import com.derppening.monikabot.impl.RNGService.computeRNGStats
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object RNG : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        val result = computeRNGStats(args)
        when (result) {
            is RNGService.Result.Failure -> {
                buildMessage(event.channel) {
                    withContent(result.message)
                }
            }
            is RNGService.Result.Success -> {
                buildEmbed(event.channel) {
                    withTitle("Probability Calculations")
                    result.embeds(this)
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `rng`")
            withDesc("Computes distribution statistics for drop tables.")
            insertSeparator()
            appendField("Usage", "```rng p=[PROBABILITY] [n=ATTEMPTS] [k=SUCCESS_TRIAL] [r=ROUND]```", false)
            appendField("`[PROBABILITY]`", "Specifies item drop chance.", false)
            appendField("`[n=ATTEMPTS]`", "Optional: Specifies number of attempts to get the item.", false)
            appendField("`[k=SUCCESSFUL_TRIAL]`", "Optional: Specifies the number of trial which you got the item.", false)
            appendField("`[r=ROUND]`", "Optional: Specifies rounding. You may use dp to signify decimal places and " +
                    "sf to signify significant figures. \nDefaults to 3 decimal places.", false)

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