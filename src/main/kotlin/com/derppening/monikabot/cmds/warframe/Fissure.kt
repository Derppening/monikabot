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
import com.derppening.monikabot.cmds.Warframe
import com.derppening.monikabot.cmds.Warframe.formatDuration
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.BuilderHelper
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object Fissure : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        val fissures = try {
            Warframe.worldState.activeMissions
        } catch (e: NoSuchElementException) {
            BuilderHelper.buildMessage(event.channel) {
                withContent("Unable to retrieve fissure missions! Please try again later.")
            }

            return Parser.HandleState.HANDLED
        }

        event.channel.toggleTypingStatus()
        BuilderHelper.buildEmbed(event.channel) {
            withTitle("Ongoing Fissures")

            fissures.sortedBy { it.modifier }.forEach {
                val nodeName = WorldState.getSolNode(it.node).value
                val missionType = WorldState.getMissionType(it.missionType)
                val tier = WorldState.getFissureModifier(it.modifier)
                val durationToExpiry = Duration.between(Instant.now(), it.expiry.date.numberLong).formatDuration()

                appendField("$tier $missionType on $nodeName", "Time Left: $durationToExpiry", false)
            }

            withTimestamp(Instant.now())
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        BuilderHelper.buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-fissure`")
            withDesc("Displays ongoing fissure missions.")
            insertSeparator()
            appendField("Usage", "```warframe fissures```", false)

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