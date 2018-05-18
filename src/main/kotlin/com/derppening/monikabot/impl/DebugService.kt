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

import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.LocationUtils.parseChannel
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.toEmbedObject
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.internal.json.objects.EmbedObject
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

    fun pipeMessageToChannel(args: List<String>, client: IDiscordClient): Boolean {
        if (args.none { it == ">>" }) {
            return false
        }

        val (messageID, channel) = args.joinToString(" ").split(">>").map {
            it.trim()
        }.let {
            check(it.size == 2) { "Incorrect number of arguments: Expected 2, got ${it.size}" }
            it[0].toLong() to it[1]
        }
        val message = client.getMessageByID(messageID).also {
            logger.debug(Core.getMethodName()) {"Message ID = $messageID\tMessage Is Null? = ${it == null}"}
            checkNotNull(it)
        }.copy()
        val parsedChannel = parseChannel(channel)

        if (message.embeds.isNotEmpty()) {
            message.embeds.forEachIndexed { index, iEmbed ->
                logger.infoFun(Core.getMethodName()) { "[$index] $iEmbed" }
            }
            parsedChannel.sendMessage(message.content, message.embeds[0].toEmbedObject())
        } else {
            parsedChannel.sendMessage(message.content)
        }

        return true
    }

    fun displayMemoryUsage(): EmbedObject {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) to runtime.totalMemory()
        val allocated = runtime.maxMemory()
        val usedPercent = (used.first.toDouble() / used.second.toDouble() * 100).toInt()
        val allocatedPercent = (used.second.toDouble() / allocated.toDouble() * 100).toInt()

        return buildEmbed {
            withTitle("Memory Usage")

            appendField("Used", "${byteToMiB(used.first)}/${byteToMiB(used.second)} MiB ($usedPercent%)", false)
            appendField("Allocated", "${byteToMiB(allocated)} MiB ($allocatedPercent%)", false)
        }.build()
    }

    private fun byteToMiB(byte: Long) = byte / 1024 / 1024
}