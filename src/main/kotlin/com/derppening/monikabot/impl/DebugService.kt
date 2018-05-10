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

package com.derppening.monikabot.impl

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.helpers.EmbedHelper.toEmbedObject
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.DiscordException

object DebugService : ILogger {
    fun appendToMessage(args: List<String>, client: IDiscordClient): Boolean {
        val id = args[0].toLongOrNull() ?: 0

        val message = client.getMessageByID(id)
        if (message == null) {
            log(ILogger.LogLevel.ERROR, "Cannot find message with ID $id")
            return false
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
                return false
            }
        } else {
            val text = args.drop(1).joinToString(" ")
            try {
                message.edit(message.content + text)
            } catch (e: DiscordException) {
                log(ILogger.LogLevel.ERROR, "Unable to edit message") {
                    stackTrace { e.stackTrace }
                }
                return false
            }
        }

        return true
    }

    fun editMessage(args: List<String>, client: IDiscordClient): Boolean {
        val id = args[0].toLongOrNull() ?: 0
        val editText = args.drop(1).joinToString(" ")

        try {
            client.getMessageByID(id)?.edit(editText)
                ?: run {
                    log(ILogger.LogLevel.ERROR, "Cannot find message with ID $id")
                    return false
                }
        } catch (e: DiscordException) {
            log(ILogger.LogLevel.ERROR, "Unable to edit message") {
                stackTrace { e.stackTrace }
            }
            return false
        }

        return true
    }

    fun longOperation(duration: Long = 10000L) {
        logger.debug("longop started")
        Thread.sleep(duration)
        logger.debug("longop ended")
    }
}