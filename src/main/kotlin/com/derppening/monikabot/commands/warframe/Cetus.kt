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
import com.derppening.monikabot.impl.warframe.CetusService.getBountyEmbeds
import com.derppening.monikabot.impl.warframe.CetusService.getGhoulEmbeds
import com.derppening.monikabot.impl.warframe.CetusService.getPlagueStarEmbeds
import com.derppening.monikabot.impl.warframe.CetusService.getTimeEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Cetus : IBase, ILogger {
    override fun cmdName(): String = "warframe-cetus"

    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        try {
            when {
                args.isEmpty() -> getBounties(event)
                "time".startsWith(args[0]) -> getTime(event)
                "ghouls".startsWith(args[0]) -> getGhoulBounties(event, true)
                "plaguestar".startsWith(args[0]) -> getPlagueStarInfo(event, true)
                else -> help(event, false)

            }
        } catch (e: Exception) {
            buildMessage(event.channel) {
                content {
                    withContent("Warframe is currently updating its information. Please be patient!")
                }
            }

            log(ILogger.LogLevel.ERROR, e.message ?: "Unknown Exception")
        }

        return Parser.HandleState.HANDLED
    }

    private fun getBounties(event: MessageReceivedEvent) {
        getPlagueStarInfo(event)

        getBountyEmbeds().forEach {
            sendEmbed(it to event.channel)
        }

        getGhoulBounties(event)
    }

    private fun getGhoulBounties(event: MessageReceivedEvent, isDirectlyInvoked: Boolean = false) {
        getGhoulEmbeds().also {
            if (it.isEmpty() && isDirectlyInvoked) {
                buildMessage(event.channel) {
                    content {
                        withContent("There are currently no Ghoul Bounties!")
                    }
                }
            }
        }.forEach {
            sendEmbed(it to event.channel)
        }
    }

    private fun getPlagueStarInfo(event: MessageReceivedEvent, isDirectlyInvoked: Boolean = false) {
        getPlagueStarEmbeds().also {
            if (it.isEmpty() && isDirectlyInvoked) {
                buildMessage(event.channel) {
                    content {
                        withContent("Operation: Plague Star is not active!")
                    }
                }
            }
        }.forEach {
            sendEmbed(it to event.channel)
        }
    }

    private fun getTime(event: MessageReceivedEvent) {
        sendEmbed(getTimeEmbed() to event.channel)
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("warframe-cetus", event) {
            description { "Displays Cetus-related information." }
            usage("warframe cetus [ghoul|plaguestar") {
                def("ghoul") { "If appended, shows ongoing Ghoul bounties." }
                def("plaguestar") { "If appended, shows ongoing Operation: Plague Star information." }
            }
            usage("warframe cetus time") {
                def("time") { "Show the current time in Cetus/Plains." }
            }
        }
    }
}