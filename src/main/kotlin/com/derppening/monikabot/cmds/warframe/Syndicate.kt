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
import com.derppening.monikabot.impl.warframe.SyndicateService.findSyndicateFromTag
import com.derppening.monikabot.impl.warframe.SyndicateService.toEmbed
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Syndicate : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        when {
            args.isNotEmpty() -> {
                getMissionsForSyndicate(event)
            }
            else -> {
                help(event, false)
            }
        }


        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-syndicate`")
            withDesc("Displays missions of a given syndicate.")
            insertSeparator()
            appendField("Usage", "```warframe syndicate [syndicate]```", false)
            appendField("`[syndicate]`", "The syndicate to show missions for.", false)

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
     * Retrieves and outputs a list of missions for a given syndicate.
     */
    private fun getMissionsForSyndicate(event: MessageReceivedEvent) {
        val args = getArgumentList(event.message.content).drop(1).joinToString(" ")

        val syndicate = findSyndicateFromTag(args).also {
            if (it.size > 1) {
                buildMessage(event.channel) {
                    withContent("The given syndicate name matches more than one syndicate!")
                    appendContent("\nYour provided syndicate name matches: \n" +
                            it.joinToString("\n") { "- ${WorldState.getSyndicateName(it.tag)}" })
                }
                return
            } else if (it.isEmpty()) {
                buildMessage(event.channel) {
                    withContent("The given syndicate name doesn't match any syndicate!")
                }
                return
            }
        }.first()

        event.channel.sendMessage(syndicate.toEmbed())
    }
}