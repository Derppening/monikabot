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

package com.derppening.monikabot.commands.warframe

import com.derppening.monikabot.commands.IBase
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.warframe.PrimeService.getCurrentPrimesStr
import com.derppening.monikabot.impl.warframe.PrimeService.getPredictedPrimesStr
import com.derppening.monikabot.impl.warframe.PrimeService.getReleasedPrimesStr
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.insertSeparator
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Prime : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        if (args.size > 1) {
            help(event, false)
            return Parser.HandleState.HANDLED
        }

        displayPrimes(args, event)

        return Parser.HandleState.HANDLED
    }

    private fun displayPrimes(args: List<String>, event: MessageReceivedEvent) {
        val listSize = if (args.isNotEmpty()) {
            try {
                args[0].toInt()
            } catch (e: NumberFormatException) {
                buildMessage(event.channel) {
                    content {
                        withContent("The number of primes to show is not an integer!")
                    }
                }
                return
            }
        } else {
            5
        }

        buildMessage(event.channel) {
            content {
                if (args.isNotEmpty()) {
                    appendContent("Released Primes: ")
                    appendContent(getReleasedPrimesStr(listSize).joinToString(""))
                } else {
                    appendContent("Current Primes:")
                    appendContent(getCurrentPrimesStr().joinToString(""))
                }
            }
        }

        buildMessage(event.channel) {
            content {
                appendContent("**[PREDICTED]** Upcoming Primes:")
                appendContent(getPredictedPrimesStr(listSize).joinToString(""))
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            fields {
                withTitle("Help Text for `warframe-prime`")
                withDesc("Displays the most recently released primes, as well as predicts the next few primes.")
                insertSeparator()
                appendField("Usage", "```warframe primes [num_to_show]```", false)
                appendField("`[num_to_show]", "Number of released/predicted primes to show.", false)
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