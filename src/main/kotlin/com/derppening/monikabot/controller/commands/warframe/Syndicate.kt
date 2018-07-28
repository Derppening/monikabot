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
import com.derppening.monikabot.impl.warframe.SyndicateService.findSyndicateFromTag
import com.derppening.monikabot.impl.warframe.SyndicateService.toEmbed
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Syndicate : IBase, ILogger {
    override fun cmdName(): String = "warframe-syndicate"
    override fun cmdInvocationAlias(): List<String> = listOf("warframe syndicates")

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        when {
            args.isNotEmpty() -> {
                getMissionsForSyndicate(event)
            }
            else -> {
                help(event, false)
            }
        }

        return CommandInterpreter.HandleState.HANDLED
    }

    /**
     * Retrieves and outputs a list of missions for a given syndicate.
     */
    private fun getMissionsForSyndicate(event: MessageReceivedEvent) {
        val args = getArgumentList(event.message.content).drop(1).joinToString(" ")

        val syndicate = findSyndicateFromTag(args).also {
            if (it.size > 1) {
                buildMessage(event.channel) {
                    content {
                        withContent("The given syndicate name matches more than one syndicate!")
                        appendContent("\nYour provided syndicate name matches: \n" +
                                it.joinToString("\n") { "- ${WorldState.getSyndicateName(it.tag)}" })
                    }
                }
                return
            } else if (it.isEmpty()) {
                buildMessage(event.channel) {
                    content {
                        withContent("The given syndicate name doesn't match any syndicate!")
                    }
                }
                return
            }
        }.first()

        sendEmbed(syndicate.toEmbed() to event.channel)
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("warframe-syndicate", event) {
            description { "Displays missions of a given syndicate." }

            usage("warframe syndicate [syndicate]") {
                def("[syndicate]") { "The syndicate to show missions for." }
            }
        }
    }
}