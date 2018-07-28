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
import com.derppening.monikabot.impl.warframe.BaroService
import com.derppening.monikabot.impl.warframe.BaroService.getBaroEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Baro : IBase, ILogger {
    override fun cmdName(): String = "warframe-baro"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        if (!BaroService.isBaroInWorldState()) {
            buildMessage(event.channel) {
                content {
                    withContent("Unable to retrieve Baro Ki'Teer information! Please try again later.")
                }
            }

            return CommandInterpreter.HandleState.HANDLED
        }

        event.channel.typingStatus = true
        sendEmbed(getBaroEmbed() to event.channel)

        return CommandInterpreter.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("warframe-baro", event) {
            description { "Displays information about Baro Ki'Teer." }

            usage("warframe baro")
        }
    }
}