/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * RTLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RTLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RTLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package core

import cmds.Config
import core.Persistence.client
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.util.DiscordException
import java.util.*
import kotlin.concurrent.thread

/**
 * A singleton IDiscordClient object.
 */
object Client : IChannelLogger, IDiscordClient by client {
    /**
     * Enumeration for bot status.
     */
    enum class Status {
        ONLINE,
        IDLE,
        BUSY,
        OFFLINE
    }

    /**
     * Listener for ReadyEvent.
     */
    @EventSubscriber
    fun onReadyListener(event: ReadyEvent) {
        try {
            event.client.changeUsername(defaultUserName)
            setStatus(defaultState, defaultStatus)

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
     * Sets the bot's status and playing message.
     *
     * @param status Status of the bot.
     * @param playingText Playing message of the bot.
     */
    fun setStatus(status: Status, playingText: String = "") {
        if (playingText != "") {
            when (status) {
                Status.ONLINE -> client.online(playingText)
                Status.IDLE -> client.idle(playingText)
                Status.BUSY -> client.dnd(playingText)
                Status.OFFLINE -> client.invisible()
            }
        } else {
            when (status) {
                Status.ONLINE -> client.online()
                Status.IDLE -> client.idle()
                Status.BUSY -> client.dnd()
                Status.OFFLINE -> client.invisible()
            }
        }
    }

    /**
     * Resets the bot's status and playing message to the default.
     */
    fun resetStatus() {
        setStatus(defaultState, defaultStatus)
    }

    /**
     * List of all background timers.
     */
    private val timers = mutableListOf<Timer>()

    /**
     * Default user name.
     */
    private const val defaultUserName = "MonikaBot"
    /**
     * Default status.
     */
    private val defaultState = Status.ONLINE
    /**
     * Default playing text.
     */
    private const val defaultStatus = "Okay Everyone!"
}
