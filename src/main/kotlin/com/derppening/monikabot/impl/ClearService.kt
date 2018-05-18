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
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.DiscordException
import kotlin.system.measureTimeMillis

object ClearService : ILogger {
    fun clearChannel(channel: IChannel, isClearAll: Boolean): Result {
        if (channel.isPrivate) {
            return Result.FAILURE_PRIVATE_CHANNEL
        }

        try {
            measureTimeMillis {
                when (isClearAll) {
                    true -> channel.fullMessageHistory
                    false -> channel.messageHistory
                }.also {
                    logger.infoFun(Core.getMethodName("...")) { "Deleting ${it.size} messages" }
                    channel.bulkDelete(it)
                }
            }.also { logger.infoFun(Core.getMethodName("...")) { "Deletion took $it ms" } }
        } catch (de: DiscordException) {
            de.printStackTrace()
            return Result.FAILURE_OTHER
        }

        return Result.SUCCESS
    }

    enum class Result {
        SUCCESS,
        FAILURE_PRIVATE_CHANNEL,
        FAILURE_OTHER
    }
}