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
import com.derppening.monikabot.impl.TAFService.toEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper
import com.derppening.monikabot.util.helpers.EmbedHelper.insertSeparator
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object TAF : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.size == 1) {
            toEmbed(args[0].toUpperCase()).forEach {
                sendEmbed(it to event.channel)
            }
        } else {
            help(event, false)
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        EmbedHelper.buildEmbed(event.channel) {
            fields {
                withTitle("Help Text for `TAF`")
                withDesc("Displays the Terminal Aerodome Forecast (TAF) of a given airfield.")
                insertSeparator()
                appendField("Usage", "```taf [icao]```", false)
                appendField("`[icao]`", "The ICAO code of the airfield to display meteorological predictions.", false)
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