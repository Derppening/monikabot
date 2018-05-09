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
import com.derppening.monikabot.impl.warframe.AlertService.getAlertEmbeds
import com.derppening.monikabot.impl.warframe.AlertService.getGoalEmbeds
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Alert : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        try {
            when {
                args.isEmpty() -> {
                    getGoals(event)
                    getAlerts(event)
                }
                args[0].endsWith("alert") -> getAlerts(event)
                args[0].endsWith("special") -> getGoals(event, true)
                else -> {
                    help(event, false)
                }
            }
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent("Warframe is currently updating its information. Please be patient!")
            }
            e.printStackTrace()

            log(ILogger.LogLevel.ERROR, e.message ?: "Unknown Exception")
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-alert`")
            withDesc("Displays all currently ongoing alerts.")
            insertSeparator()
            appendField("Usage", "```warframe alert [--alert|--special]```", false)
            appendField("`--alert`", "Only show normal mission alerts.", false)
            appendField("`--special`", "Only show special alerts.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    /**
     * Retrieves and outputs a list of alerts.
     */
    private fun getAlerts(event: MessageReceivedEvent) {
        getAlertEmbeds().also {
            if (it.isEmpty()) {
                buildMessage(event.channel) {
                    withContent("There are currently no alerts!")
                }
            }
        }.forEach {
            event.channel.sendMessage(it)
        }
    }

    /**
     * Retrieves and outputs a list of special alerts ("goals").
     */
    private fun getGoals(event: MessageReceivedEvent, isDirectlyInvoked: Boolean = false) {
        getGoalEmbeds().also {
            if (isDirectlyInvoked && it.isEmpty()) {
                buildMessage(event.channel) {
                    withContent("There are currently no special alerts!")
                }
            }
        }.forEach {
            event.channel.sendMessage(it)
        }
    }
}