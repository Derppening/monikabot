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
import com.derppening.monikabot.models.warframe.Manifest
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object Darvo : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        val darvoDeal = try {
            Warframe.worldState.dailyDeals.first()
        } catch (e: NoSuchElementException) {
            buildMessage(event.channel) {
                withContent("Darvo currently has no items on sale!")
            }

            return Parser.HandleState.HANDLED
        }
        buildEmbed(event.channel) {
            withAuthorName("Darvo Sale")
            withTitle(WorldState.getLanguageFromAsset(darvoDeal.storeItem))

            appendField("Time Left", Duration.between(Instant.now(), darvoDeal.expiry.date.numberLong).formatDuration(), false)
            appendField("Price", "${darvoDeal.originalPrice}p -> ${darvoDeal.salePrice}p", true)
            appendField("Discount", "${darvoDeal.discount}%", true)
            if (darvoDeal.amountSold == darvoDeal.amountTotal) {
                appendField("Amount Left", "Sold Out", false)
            } else {
                appendField("Amount Left", "${darvoDeal.amountTotal - darvoDeal.amountSold}/${darvoDeal.amountTotal}", false)
            }

            val imageRegex = Regex(darvoDeal.storeItem.takeLastWhile { it != '/' } + '$')
            withImage(Manifest.findImageByRegex(imageRegex))

            withTimestamp(Instant.now())
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-darvo`")
            withDesc("Displays the ongoing Darvo sale.")
            insertSeparator()
            appendField("Usage", "```warframe darvo```", false)

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
