/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package core

import cmds.Config
import core.Persistence.client
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.DiscordException
import java.util.*
import kotlin.concurrent.thread

/**
 * A singleton IDiscordClient object.
 */
object Client : IChannelLogger, IDiscordClient by client {
    /**
     * Listener for ReadyEvent.
     */
    @EventSubscriber
    fun onReadyListener(event: ReadyEvent) {
        try {
            event.client.changeUsername(defaultUserName)
            changePresence(defaultState, defaultActivity, defaultText)

            thread {
                PersistentMessage.modify("Config", "Experimental Features", Config.enableExperimentalFeatures.toString())
                PersistentMessage.modify("Misc", "Version", Core.monikaVersion, true)
            }

            log(IChannelLogger.LogLevel.INFO, "Ready") {
                info { "Initialization complete with $shardCount shard(s)" }
            }
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
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
        log(IChannelLogger.LogLevel.INFO, "Successfully updated")
    }

    /**
     * List of all background timers.
     */
    private val timers = mutableListOf<Timer>()

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
}
