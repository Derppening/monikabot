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

package com.derppening.monikabot.cmds.warframe

import com.derppening.monikabot.cmds.IBase
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.warframe.MarketService
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Market : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        if (args.isEmpty()) {
            buildMessage(event.channel) {
                withContent("Please specify an item to lookup!")
            }

            return Parser.HandleState.HANDLED
        }

        event.channel.toggleTypingStatus()
        MarketService.getMarketItem(args).also {
            when (it) {
                is MarketService.Result.Failure -> {
                    buildMessage(event.channel) {
                        withContent(it.message)
                    }
                }
                is MarketService.Result.Success -> {
                    event.channel.sendMessage(it.embed)
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-market`")
            withDesc("Displays market information of any item.")
            insertSeparator()
            appendField("Usage", "```warframe market [search_expr]```", false)
            appendField("`[search_expr]`", "The search expression." +
                    "\n\nThe expression can comprise of one or more space-delimited terms:" +
                    "\n\t- `[term]`: Fuzzily match `[term]`" +
                    "\n\t- `\"[term]\"`: Match whole `[term]`" +
                    "\n\t- `*`: Match anything", false)

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