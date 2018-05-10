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
import com.derppening.monikabot.impl.warframe.InvasionService.getInvasionAlertEmbed
import com.derppening.monikabot.impl.warframe.InvasionService.getInvasionEmbeds
import com.derppening.monikabot.impl.warframe.InvasionService.getInvasionTimerEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.insertSeparator
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Invasion : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        when {
            args.isEmpty() -> getInvasionData(event)
            "timer".startsWith(args[0]) -> getInvasionTimer(event)
            else -> help(event, false)
        }

        return Parser.HandleState.HANDLED
    }

    private fun getInvasionData(event: MessageReceivedEvent) {
        getInvasionEmbeds().also {
            if (it.isEmpty()) {
                buildMessage(event.channel) {
                    content {
                        withContent("There are currently no invasions!")
                    }
                }
            }
        }.forEach {
            sendEmbed(it to event.channel)
        }
    }

    private fun getInvasionTimer(event: MessageReceivedEvent) {
        sendEmbed(getInvasionTimerEmbed() to event.channel)
        sendEmbed(getInvasionAlertEmbed() to event.channel)
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            fields {
                withTitle("Help Text for `warframe-invasion`")
                withDesc("Displays the invasion progress in Warframe.")
                insertSeparator()
                appendField("Usage", "```warframe invasion [timer]```", false)
                appendField("`timer`", "If appended, show the construction progress for Balor Fomorian and Razorback.", false)
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