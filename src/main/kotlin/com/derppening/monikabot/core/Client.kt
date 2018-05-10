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

import com.derppening.monikabot.core.Persistence.client
import com.derppening.monikabot.impl.ConfigService
import com.derppening.monikabot.impl.ReminderService
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.DiscordException
import java.util.*
import kotlin.system.exitProcess

/**
 * A singleton IDiscordClient object.
 */
object Client : ILogger, IDiscordClient by client {
    /**
     * Default activity.
     */
    private val defaultActivity = ActivityType.PLAYING
    /**
     * Default user name.
     */
    private const val defaultUserName = "MonikaBot"
    /**
     * Default state.
     */
    private val defaultState = StatusType.ONLINE
    /**
     * Default text.
     */
    private const val defaultText = "Okay Everyone!"

    /**
     * List of all background timers.
     */
    private val timers = mutableListOf<Timer>()

    @EventSubscriber
    fun onReadyListener(event: ReadyEvent) {
        try {
            event.client.changeUsername(defaultUserName)
            changePresence(defaultState, defaultActivity, defaultText)

            PersistentMessage.modify("Config", "Experimental Features", ConfigService.enableExperimentalFeatures.toString())
            PersistentMessage.modify("Config", "Owner Mode Echo for Superusers", ConfigService.ownerModeEchoForSu.toString())
            PersistentMessage.modify("Misc", "Version", Core.monikaVersion, true)

            logger.info("Initialization complete with $shardCount shard(s)")

            ReminderService.importTimersFromFile()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }

    @EventSubscriber
    fun onRconnectFailureListener(event: ReconnectFailureEvent) {
        if (event.isShardAbandoned) {
            logger.info("onReconnectFailureListener() - Attempting client reconnect")

            while (!event.client.isReady) {
                Thread.sleep(30000)
                try {
                    event.client.login()
                } catch (e: Exception) {
                }
            }
        }
    }

    @EventSubscriber
    fun onReconnectSuccessListener(event: ReconnectSuccessEvent) {
        event.client.changeUsername(defaultUserName)
        changePresence(defaultState, defaultActivity, defaultText)

        logger.info("onReconnectSuccessListener() - Initialization complete with $shardCount shard(s)")
    }

    fun logoutHandler() {
        ReminderService.exportTimersToFile()
        Client.clearTimers()

        logout()
        exitProcess(0)
    }

    /**
     * Registers [timer] as an event.
     */
    fun registerTimer(timer: Timer) {
        timers.add(timer)
    }

    /**
     * Clears all timers from [timers].
     */
    fun clearTimers() {
        timers.forEach {
            it.cancel()
        }
    }

    /**
     * Resets the bot's status and playing message to the default.
     */
    fun resetStatus() {
        changePresence(defaultState, defaultActivity, defaultText)
        log(ILogger.LogLevel.INFO, "Successfully updated")
    }
}
