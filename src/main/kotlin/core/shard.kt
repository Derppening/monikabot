package core

import getBotAdmin
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent
import sx.blah.discord.handle.impl.events.shard.ShardReadyEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

class Shard {
    @EventSubscriber
    fun onConnectListener(event: ShardReadyEvent) {
        try {
            MessageBuilder(event.client).apply {
                withChannel(event.client.fetchUser(getBotAdmin()).orCreatePMChannel)
                withCode("diff", "+ Shard ${event.shard.info[0]} connected (Total: ${event.shard.info[1]})")
            }.build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }

    @EventSubscriber
    fun onDisconnectListener(event: DisconnectedEvent) {
        val reason = when (event.reason) {
            DisconnectedEvent.Reason.LOGGED_OUT -> "Logging Out"
            DisconnectedEvent.Reason.RECONNECT_OP -> "Trying to Reconnect"
            DisconnectedEvent.Reason.INVALID_SESSION_OP -> "Invalid Session"
            DisconnectedEvent.Reason.ABNORMAL_CLOSE -> "Abnormal Closure"
            else -> "null"
        }

        val message = "- Shard ${event.shard.info[0]} of ${event.shard.info[1]} disconnected.\n\tReason: $reason"

        // TODO: Message Admin if this is not the only shard
        if (!event.shard.isReady) {
            println(message)
            return
        }

        try {
            MessageBuilder(event.client).apply {
                withChannel(event.client.fetchUser(getBotAdmin()).orCreatePMChannel)
                withCode("diff", message)
            }.build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }
}
