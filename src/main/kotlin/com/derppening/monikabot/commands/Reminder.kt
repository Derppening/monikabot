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

package com.derppening.monikabot.commands

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.ReminderService
import com.derppening.monikabot.impl.ReminderService.clear
import com.derppening.monikabot.impl.ReminderService.list
import com.derppening.monikabot.impl.ReminderService.remove
import com.derppening.monikabot.impl.ReminderService.schedule
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Instant

object Reminder : IBase, ILogger {
    override fun cmdName(): String = "reminder"

    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        when {
            args.isEmpty() -> help(event, false)
            args.any { it.matches(Regex("-{0,2}add")) } -> scheduleDelay(event)
            args.any { it.matches(Regex("-{0,2}list")) } -> listTimer(event)
            args.any { it.matches(Regex("-{0,2}remove")) } -> removeTimer(event)
            args.any { it.matches(Regex("-{0,2}clear")) } -> clearTimers(event)
            args.any { it.matches(Regex("for")) } -> scheduleDelay(event)
            else -> help(event, false)
        }

        return Parser.HandleState.HANDLED
    }

    private fun scheduleDelay(event: MessageReceivedEvent) {
        val args = getArgumentList(event.message.content)
                .toMutableList()
                .apply { removeIf { it.matches(Regex("(for|-{0,2}add)")) } }

        buildMessage(event.channel) {
            content {
                withContent(schedule(args, event.author))
            }
        }
    }

    private fun listTimer(event: MessageReceivedEvent) {
        val result = list(event.author)
        when (result) {
            is ReminderService.Result.Message -> {
                buildMessage(event.author.orCreatePMChannel) {
                    content {
                        withContent(result.message)
                    }
                }
            }
            is ReminderService.Result.Embed -> {
                buildEmbed(event.author.orCreatePMChannel) {
                    fields {
                        withTitle("Your Reminders")

                        result.embeds(this)

                        withTimestamp(Instant.now())
                    }
                }
            }
        }
    }

    private fun removeTimer(event: MessageReceivedEvent) {
        val timerName = getArgumentList(event.message.content)
                .toMutableList()
                .apply { removeIf { it.matches(Regex("-{0,2}remove")) } }
                .joinToString(" ")

        buildMessage(event.channel) {
            content {
                withContent(remove(timerName, event.author))
            }
        }
    }

    private fun clearTimers(event: MessageReceivedEvent) {
        buildMessage(event.author.orCreatePMChannel) {
            content {
                withContent(clear(event.author))
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("reminder", event) {
            description {
                "Sets a reminder for yourself." +
                        "\n**WARNING**: Do not use this timer for any mission-critical tasks. When this bot goes" +
                        "into maintenance, all timer tasks will be paused until the bot restarts. This will likely " +
                        "cause reminder delays!"
            }

            usage("reminder for [--lazy] [duration] [name]") {
                def("--lazy") { "If specified, only check if the time is in the future." }
                def("[duration]") {
                    "Any duration, in the format of `[days]d [hours]h [minutes]m [seconds]s`." +
                        "\nAny part of the duration can be truncated."
                }
                def("[name]") { "Name of the timer. All timers must have unique names." }
            }
            usage("reminder remove [name]") {
                def("[name]") { "Name of the timer to remove." }
            }
            usage("reminder [list|clear]") {
                def("list") { "Lists all ongoing reminders." }
                def("clear") { "Clears all ongoing reminders." }
            }
        }
    }
}