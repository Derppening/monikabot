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

package cmds.experimental

import cmds.IBase
import core.BuilderHelper.buildMessage
import core.Core.popLeadingMention
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.io.File
import java.nio.file.Paths

object Emoticon : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val testRegex = popLeadingMention(event.message.content, event.guild)
                .toLowerCase().replace("*", ".+").dropLastWhile { it == '!' }
        val matching = pairs.filter { it.key.matches(testRegex.toRegex()) }

        return when (matching.size) {
            0 -> {
                Parser.HandleState.NOT_FOUND
            }
            1 -> {
                buildMessage(event.channel) {
                    withContent(matching.values.first())
                }
                Parser.HandleState.HANDLED
            }
            else -> {
                buildMessage(event.author.orCreatePMChannel) {
                    withContent("Multiple Matches!\n\n")
                    appendContent(matching.entries.joinToString("\n") { "${it.key} - ${it.value}" })
                }
                Parser.HandleState.UNHANDLED
            }
        }
    }

    private fun readFromFile(): Map<String, String> =
            File(Paths.get("persistent/emoticons.txt").toUri())
                    .readLines()
                    .associateBy(
                            { it.split("=").first() },
                            { it.split("=").drop(1).joinToString("=") }
                    )

    private val pairs = readFromFile()
}