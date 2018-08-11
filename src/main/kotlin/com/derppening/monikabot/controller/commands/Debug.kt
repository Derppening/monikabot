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

package com.derppening.monikabot.controller.commands

import com.derppening.monikabot.controller.CommandInterpreter
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.DebugService.appendToMessage
import com.derppening.monikabot.impl.DebugService.displayMemoryUsage
import com.derppening.monikabot.impl.DebugService.displayMessageCache
import com.derppening.monikabot.impl.DebugService.editEmbed
import com.derppening.monikabot.impl.DebugService.editMessage
import com.derppening.monikabot.impl.DebugService.pipeMessageToChannel
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Debug : IBase, ILogger {
    override fun cmdName(): String = "debug"

    override fun handlerSu(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, true)
            return CommandInterpreter.HandleState.HANDLED
        }

        // TODO(Derppening): Add help text for all of them
        when (args[0]) {
            "embed.edit" -> {
                if (args.elementAtOrNull(1) == "--help") {
                    buildHelpText("debug embed.edit", event) {

                    }

                    return CommandInterpreter.HandleState.HANDLED
                }

                editEmbed(args.drop(1), event.client)
            }
            "message.append" -> {
                appendToMessage(args.drop(1), event.client)
            }
            "message.edit" -> {
                editMessage(args.drop(1), event.client)
            }
            "message.pipe.channel" -> {
                pipeMessageToChannel(args.drop(1), event.client)
            }
            "sys.mem" -> {
                sendEmbed(displayMemoryUsage() to event.channel)
            }
            "sys.messageCache" -> {
                sendEmbed(displayMessageCache(event.client) to event.channel)
            }
            else -> {
                logToChannel(ILogger.LogLevel.ERROR, "Unknown debug option \"${args[0]}\"") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
        }

        return CommandInterpreter.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("debug", event) {
            description { "Enables superuser debugging methods." }

            usage("debug [option] [args]") {
                field("Option: `embed.edit`") { "Edits a field of an embed." }
                field("Option: `message.append`") { "Appends to a message." }
                field("Option: `message.edit`") { "Edits a message." }
                field("Option: `message.pipe.channel`") { "Copies a message to another channel." }
                field("Option: `sys.mem`") { "Displays current memory usage." }
                field("Option: `sys.messageCache`") { "Displays message cache usage." }
            }
        }
    }
}
