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

package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.io.File

object Changelog : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        val changes = readChangelog()

        val showRelease = args.any { it.matches(Regex("-{0,2}release")) }
        val showAllChanges = args.any { it.matches(Regex("-{0,2}all")) }

        if (showAllChanges) {
            outputAllChanges(event, changes, showRelease)
        } else {
            outputLatestChanges(event, changes, showRelease)
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `changelog`")
            withDesc("Displays the changelog for the most recent build(s).")
            insertSeparator()
            appendField("Usage", "```changelog [release] [all]```", false)
            appendField("`release`", "Only show changes for release builds.", false)
            appendField("`all`", "Show 5 most recent builds instead of 1.", false)

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
     * Displays the changes of the most recent 5 builds.
     */
    private fun outputAllChanges(event: MessageReceivedEvent, changes: List<Pair<String, List<String>>>, showRel: Boolean) {
        val displayChanges = if (showRel) {
            changes.filterNot { (k, _) -> k.contains('-') }
        } else {
            changes
        }.takeLast(5)

        buildEmbed(event.channel) {
            withTitle("Last 5 Changelogs")
            if (displayChanges.isEmpty()) {
                withDesc("There are no official releases (yet)!")
            } else {
                displayChanges.forEach { (ver, changetext) ->
                    appendField(ver, changetext.joinToString("\n"), false)
                }
            }
        }
    }

    /**
     * Displays the changes of the most recent build.
     */
    private fun outputLatestChanges(event: MessageReceivedEvent, changes: List<Pair<String, List<String>>>, showRel: Boolean) {
        val displayChange: Pair<String, List<String>>

        try {
            displayChange = if (showRel) {
                changes.filterNot { (k, _) -> k.contains('-') }
            } else {
                changes
            }.last()
        } catch (nsee: NoSuchElementException) {
            buildMessage(event.channel) {
                withContent("There are no official releases (yet)!")
            }
            return
        }

        buildEmbed(event.channel) {
            withTitle("Changelog for ${displayChange.first}")
            withDesc(displayChange.second.joinToString("\n"))
        }
    }

    /**
     * Reads and returns the change log of the bot.
     */
    private fun readChangelog(): List<Pair<String, List<String>>> {
        val contents = File(Thread.currentThread().contextClassLoader.getResource("lang/Changelog.md").toURI()).readLines()

        val logMap = mutableMapOf<String, MutableList<String>>()
        var ver = ""
        for (line in contents) {
            if (line.startsWith('[') && line.endsWith(']')) {
                ver = line.substring(1, line.lastIndex)
                logMap[ver] = mutableListOf()
            } else if (ver.isNotBlank()) {
                logMap[ver]?.add(line)
            }
        }

        return logMap.toList()
    }
}