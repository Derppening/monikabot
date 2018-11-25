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
                        description { "Edits a field of an embed." }

                        usage("[ID] [FIELD_KEY] [NEW_KEY] [NEW_VALUE]") {
                            option("ID") { "ID of the embed message." }
                            option("FIELD_KEY") { "Key (i.e. header) of field to edit." }
                            option("NEW_KEY") { "New value for field header. Use '_' if keeping the original value." }
                            option("NEW_VALUE") { "New value for field content. Use '_' if keeping the original value." }
                        }

                        usage("[ID] [FIELD_KEY] _ _") {
                            desc { "Special invocation: Removes a field from the embed." }
                            option("ID") { "ID of the embed message." }
                            option("FIELD_KEY") { "Key (i.e. header) of field to edit." }
                        }
                    }

                    return CommandInterpreter.HandleState.HANDLED
                }

                editEmbed(args.drop(1), event.client)
            }
            "message.append" -> {
                if (args.elementAtOrNull(1) == "--help") {
                    buildHelpText("debug message.append", event) {
                        description { "Appends text to a message/embed." }

                        usage("[ID] key=[KEY] val=[VAL]") {
                            desc { "Appends a field to an embed." }

                            option("ID") {
                                "ID of the message containing an embed. If the message does not contain an embed, " +
                                        "will instead directly append to the message contents."
                            }
                            option("KEY") { "Header of the embed field." }
                            option("VAL") { "Content of the embed field." }
                        }

                        usage("[ID] [MESSAGE]...") {
                            desc { "Appends text to a message." }

                            option("ID") { "ID of the message." }
                            option("MESSAGE") { "Content of the message to append." }
                        }
                    }

                    return CommandInterpreter.HandleState.HANDLED
                }

                appendToMessage(args.drop(1), event.client)
            }
            "message.edit" -> {
                if (args.elementAtOrNull(1) == "--help") {
                    buildHelpText("debug message.edit", event) {
                        description { "Edits the text of a message." }

                        usage("[ID] [MESSAGE]...") {
                            option("ID") { "ID of the message." }
                            option("MESSAGE") { "Content of the message to replace." }
                        }
                    }

                    return CommandInterpreter.HandleState.HANDLED
                }

                editMessage(args.drop(1), event.client)
            }
            "message.pipe.channel" -> {
                if (args.elementAtOrNull(1) == "--help") {
                    buildHelpText("debug message.pipe.channel", event) {
                        description { "Copies a message to another channel." }

                        usage("[MESSAGE_ID] >> [CHANNEL_ID]") {
                            option("MESSAGE_ID") { "ID of message to copy." }
                            option("CHANNEL_ID") { "ID of destination channel." }
                        }
                    }

                    return CommandInterpreter.HandleState.HANDLED
                }

                pipeMessageToChannel(args.drop(1), event.client)
            }
            "sys.mem" -> {
                if (args.elementAtOrNull(1) == "--help") {
                    buildHelpText("debug sys.mem", event) {
                        description { "Displays information about this bot's memory usage." }

                        usage()
                    }

                    return CommandInterpreter.HandleState.HANDLED
                }

                sendEmbed(displayMemoryUsage() to event.channel)
            }
            "sys.messageCache" -> {
                if (args.elementAtOrNull(1) == "--help") {
                    buildHelpText("debug sys.messageCache", event) {
                        description { "Displays information about the message cache." }

                        usage()
                    }

                    return CommandInterpreter.HandleState.HANDLED
                }

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

            usage("[option] [args]") {
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
