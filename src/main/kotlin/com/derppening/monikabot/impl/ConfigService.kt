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
import com.derppening.monikabot.core.PersistentMessage
import kotlin.concurrent.thread

object ConfigService : ILogger {
    /**
     * Whether to enable experimental features.
     */
    var enableExperimentalFeatures = Core.monikaVersionBranch == "development"
        private set
    /**
     * Whether to allow superusers to access owner mode echo.
     */
    var ownerModeEchoForSu = true
        private set

    enum class Result {
        GET,
        SET,
        HELP
    }

    fun configureExperimentalFlag(args: List<String>): Result {
        if (args.size == 1) {
            return Result.GET
        } else if (args.size != 2 || args[1].matches(Regex("-{0,2}help"))) {
            return Result.HELP
        }

        enableExperimentalFeatures = args[1].toBoolean() || args[1] == "enable"

        thread {
            PersistentMessage.modify("Config", "Experimental Features", enableExperimentalFeatures.toString(), true)
        }

        return Result.SET
    }

    fun configureOwnerEchoFlag(args: List<String>): Result {
        if (args.size == 1) {
            return Result.GET
        } else if (args.size != 2 || args[1].matches(Regex("-{0,2}help"))) {
            return Result.HELP
        }

        ownerModeEchoForSu = args[1].toBoolean() || args[1] == "allow"

        thread {
            PersistentMessage.modify("Config", "Owner Mode Echo for Superusers", ownerModeEchoForSu.toString(), true)
        }

        return Result.SET
    }
}