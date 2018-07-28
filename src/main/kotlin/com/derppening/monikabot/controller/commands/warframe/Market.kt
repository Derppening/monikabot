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

package com.derppening.monikabot.controller.commands.warframe

import com.derppening.monikabot.controller.CommandInterpreter
import com.derppening.monikabot.controller.commands.IBase
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.warframe.MarketService
import com.derppening.monikabot.impl.warframe.MarketService.getMarketItem
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Market : IBase, ILogger {
    override fun cmdName(): String = "warframe-market"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        if (args.isEmpty()) {
            buildMessage(event.channel) {
                content {
                    withContent("Please specify an item to lookup!")
                }
            }

            return CommandInterpreter.HandleState.HANDLED
        }

        event.channel.typingStatus = true
        getMarketItem(args).also {
            when (it) {
                is MarketService.Result.Failure -> {
                    buildMessage(event.channel) {
                        content {
                            withContent(it.message)
                        }
                    }
                }
                is MarketService.Result.Success -> {
                    sendEmbed(it.embed to event.channel)
                }
            }
        }

        return CommandInterpreter.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) { buildHelpText("warframe-market", event) {
            description { "Displays market information of any item." }

            usage("warframe market [search_expr]") {
                def("[search_expr]") {
                    "The search expression." +
                            "\n\nThe expression can comprise of one or more space-delimited terms:" +
                            "\n\t- `[term]`: Fuzzily match `[term]`" +
                            "\n\t- `\"[term]\"`: Match whole `[term]`" +
                            "\n\t- `*`: Match anything"
                }
            }
        }
    }
}