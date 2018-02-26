package core

import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.shard.ShardReadyEvent
import sx.blah.discord.util.DiscordException

/**
 * Singleton housing all shard-related operations.
 */
object Shard {
    /**
     * Listener when a shard is connected.
     *
     * @param event
     */
    @EventSubscriber
    fun onConnectListener(event: ShardReadyEvent) {
        try {
            Log.updatePersistent()
            Log.plus(javaClass.name,
                    "Shard ${event.shard.info[0]} connected (Total: ${event.shard.info[1]})")
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }
}
