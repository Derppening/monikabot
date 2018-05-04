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
import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.ILogger
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType

object StopService : ILogger {
    fun cleanup(isForced: Boolean) {
        if (!isForced && Core.monikaVersionBranch == "stable") {
            Client.changePresence(StatusType.DND, ActivityType.PLAYING, "Maintenance")
            if (TriviaService.users.isNotEmpty()) {
                log(ILogger.LogLevel.INFO, "Sending shutdown messages to all Trivia players...")
                TriviaService.gracefulShutdown()
            }
            Thread.sleep(60000)
        }

        Client.logoutHandler()
    }
}