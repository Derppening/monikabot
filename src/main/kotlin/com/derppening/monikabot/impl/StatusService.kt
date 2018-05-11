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

import com.derppening.monikabot.core.Client
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.MessageUtils.removeQuotes
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType

object StatusService : ILogger {
    fun setNewStatus(args: MutableList<String>) {
        if (args.size == 0) {
            Client.resetStatus()

            return
        }

        val status = when (args[0]) {
            "--reset" -> {
                Client.resetStatus()
                return
            }
            "--idle" -> {
                args.removeAt(0)
                StatusType.IDLE
            }
            "--dnd", "--busy" -> {
                args.removeAt(0)
                StatusType.DND
            }
            "--offline", "--invisible" -> {
                args.removeAt(0)
                StatusType.INVISIBLE
            }
            "--online" -> {
                args.removeAt(0)
                StatusType.ONLINE
            }
            else -> StatusType.ONLINE
        }

        val activity = when (args[0]) {
            "--play", "--playing" -> {
                args.removeAt(0)
                ActivityType.PLAYING
            }
            "--stream", "--streaming" -> {
                args.removeAt(0)
                ActivityType.STREAMING
            }
            "--listen", "--listening" -> {
                args.removeAt(0)
                ActivityType.LISTENING
            }
            "--watch", "--watching" -> {
                args.removeAt(0)
                ActivityType.WATCHING
            }
            else -> ActivityType.PLAYING
        }

        val arg = args.joinToString(" ").removeQuotes()
        val streamUrl = arg.substringAfter("--").trim().dropWhile { it == '<' }.dropLastWhile { it == '>' }
        val message = arg.let {
            if (activity == ActivityType.STREAMING) {
                it.substringBefore("--").trim()
            } else {
                it
            }
        }

        try {
            if (activity == ActivityType.STREAMING) {
                Client.changeStreamingPresence(status, message, streamUrl)
            } else {
                Client.changePresence(status, activity, message)
            }
            log(ILogger.LogLevel.INFO, "Successfully updated")
        } catch (e: Exception) {
            log(ILogger.LogLevel.ERROR, "Cannot set status") {
                info { e.message ?: "Unknown Exception" }
            }
            e.printStackTrace()
        }
    }
}