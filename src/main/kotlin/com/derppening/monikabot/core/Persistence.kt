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

package com.derppening.monikabot.core

import com.derppening.monikabot.core.Core.ownerPrivateChannel
import com.derppening.monikabot.core.Core.privateKey
import com.derppening.monikabot.core.Core.serverDebugChannel
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.DiscordException
import kotlin.system.exitProcess

/**
 * Singleton housing all persistent objects.
 */
object Persistence : ILogger {
    /**
     * Core IDiscordClient object.
     */
    val client: IDiscordClient by lazy {
        val builder = ClientBuilder().withToken(privateKey)
        try {
            builder.login()
        } catch (e: DiscordException) {
            e.printStackTrace()
            throw Exception("Unable to instantiate client")
        }
    }

    /**
     * Debug channel.
     */
    val debugChannel: IChannel by lazy {
        try {
            serverDebugChannel.apply {
                if (Core.monikaVersionBranch == "stable" && this?.fullMessageHistory?.isNotEmpty() == true) {
                    bulkDelete()
                }
            } ?: ownerPrivateChannel
        } catch (e: Exception) {
            logger.error("Cannot initialize debug channel")
            e.printStackTrace()

            exitProcess(0)
        }
    }
}
