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
import com.derppening.monikabot.impl.DebugService.appendToMessage
import com.derppening.monikabot.impl.DebugService.editMessage
import com.derppening.monikabot.impl.DebugService.longOperation
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Debug : IBase, ILogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, true)
            return Parser.HandleState.HANDLED
        }

        when (args[0].toLowerCase()) {
            "append" -> {
                appendToMessage(args.drop(1), event.client)
            }
            "edit" -> {
                editMessage(args.drop(1), event.client)
            }
            "longop" -> {
                longOperation()
            }
            "mem" -> {
                val runtime = Runtime.getRuntime()
                val byteToMiB = { byte: Long ->
                    byte / 1024 / 1024
                }
                val used = (runtime.totalMemory() - runtime.freeMemory()) to runtime.totalMemory()
                val allocated = runtime.maxMemory()
                val usedPercent = (used.first.toDouble() / used.second.toDouble() * 100).toInt()
                val allocatedPercent = (used.second.toDouble() / allocated.toDouble() * 100).toInt()

                buildEmbed(event.channel) {
                    fields {
                        withTitle("Memory Usage")

                        appendField("Used", "${byteToMiB(used.first)}/${byteToMiB(used.second)} MiB ($usedPercent%)", false)
                        appendField("Allocated", "${byteToMiB(allocated)} MiB ($allocatedPercent%)", false)
                    }
                }
            }
            else -> {
                log(ILogger.LogLevel.ERROR, "Unknown debug option \"${args[0]}\"") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            fields {
                withTitle("Help Text for `debug`")
                withDesc("Enables superuser debugging methods.")
            }

            onError {
                discordException { e ->
                    log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                        author { event.author }
                        channel { event.channel }
                        info { e.errorMessage }
                    }
                }
            }
        }
    }
}
