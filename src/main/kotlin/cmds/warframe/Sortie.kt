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

package cmds.warframe

import cmds.IBase
import cmds.Warframe
import cmds.Warframe.formatDuration
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import models.warframe.worldstate.WorldState
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object Sortie : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        val sortie = try {
            Warframe.worldState.sorties.first()
        } catch (e: NoSuchElementException) {
            buildMessage(event.channel) {
                withContent("Unable to retrieve sortie information! Please try again later.")
            }
            e.printStackTrace()

            return Parser.HandleState.HANDLED
        }
        buildEmbed(event.channel) {
            val boss = WorldState.getSortieBoss(sortie.boss)
            withTitle("Sortie Information")

            appendField("Expires in", Duration.between(Instant.now(), sortie.expiry.date.numberLong).formatDuration(), false)
            appendField("Boss", "${boss.name} (${boss.faction})", false)
            sortie.variants.forEachIndexed { i, m ->
                val missionType = WorldState.getMissionType(m.missionType)
                val modifier = WorldState.getSortieModifier(m.modifierType)
                val node = WorldState.getSolNode(m.node)
                appendField("Mission ${i + 1} - $missionType on ${node.value}", "Modifier: ${modifier.type}", true)
            }

            withTimestamp(Instant.now())
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-sortie`")
            withDesc("Displays the current sorties.")
            insertSeparator()
            appendField("Usage", "```warframe sorties```", false)

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
