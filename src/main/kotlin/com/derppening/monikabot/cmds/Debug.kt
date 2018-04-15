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

package com.derppening.monikabot.cmds

import com.derppening.monikabot.core.BuilderHelper.buildEmbed
import com.derppening.monikabot.core.BuilderHelper.buildMessage
import com.derppening.monikabot.core.BuilderHelper.toEmbedObject
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Debug : IBase, ILogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, true)
            return Parser.HandleState.HANDLED
        }

        when (args[0].toLowerCase()) {
            "append" -> {
                val args = args.drop(1)
                val id = args[0].toLongOrNull() ?: 0

                val message = event.client.getMessageByID(id)
                if (message == null) {
                    buildMessage(event.channel) {
                        withContent("Cannot find message with ID $id")
                    }
                    return Parser.HandleState.HANDLED
                }

                if (message.embeds.isNotEmpty()) {
                    val key = args.find { it.startsWith("key=", true) }?.removePrefix("key=") ?: ""
                    val value = args.find { it.startsWith("val=", true) }?.removePrefix("val=") ?: ""

                    val from = message.embeds.first()
                    val to = from.toEmbedObject {
                        appendField(key, value, false)
                    }
                    try {
                        message.edit(to)
                    } catch (e: DiscordException) {
                        log(ILogger.LogLevel.ERROR, "Unable to edit message") {
                            stackTrace { e.stackTrace }
                        }
                    }
                } else {
                    val text = args.drop(1).joinToString(" ")
                    try {
                        message.edit(message.content + text)
                    } catch (e: DiscordException) {
                        log(ILogger.LogLevel.ERROR, "Unable to edit message") {
                            stackTrace { e.stackTrace }
                        }
                    }
                }
            }
            "edit" -> {
                val args = args.drop(1)
                val id = args[0].toLongOrNull() ?: 0
                val editText = args.drop(1).joinToString(" ")

                try {
                    event.client.getMessageByID(id)?.edit(editText) ?: buildMessage(event.channel) {
                        withContent("Cannot find message with ID $id")
                    }
                } catch (e: DiscordException) {
                    log(ILogger.LogLevel.ERROR, "Unable to edit message") {
                        stackTrace { e.stackTrace }
                    }
                }
            }
            "longop" -> {
                logger.debug("longop started")
                Thread.sleep(10000)
                logger.debug("longop ended")
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
            withTitle("Help Text for `debug`")
            withDesc("Enables superuser debugging methods.")

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
