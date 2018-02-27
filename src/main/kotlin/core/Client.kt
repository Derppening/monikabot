package core

import core.Persistence.client
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.util.DiscordException
import java.util.*

/**
 * A singleton IDiscordClient object.
 */
object Client : IDiscordClient by client {
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

            Log.plus(javaClass.name, "Ready", info = "Initialization complete with $shardCount shard(s)")

            Log.modifyPersistent("Misc", "Version", Core.monikaVersion, true)
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
    private val defaultState = Status.IDLE
    /**
     * Default playing text.
     */
    private const val defaultStatus = "I'm still learning (>.<)"
}
