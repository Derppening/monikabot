package core

import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.shard.ShardReadyEvent
import sx.blah.discord.util.DiscordException

class Shard {
    @EventSubscriber
    fun onConnectListener(event: ShardReadyEvent) {
        try {
            Log.plus("Shard ${event.shard.info[0]} connected (Total: ${event.shard.info[1]})")
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }
}
