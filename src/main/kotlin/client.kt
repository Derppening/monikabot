import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

private val client by lazy {
    val builder = ClientBuilder().withToken(getPrivateKey())
    try {
        builder.login()
    } catch (e: DiscordException) {
        e.printStackTrace()
        throw Exception("Unable to instantiate Client")
    }
}

object Client : IDiscordClient by client {
    enum class Status {
        ONLINE,
        IDLE,
        BUSY,
        OFFLINE
    }

    @EventSubscriber
    fun onReadyListener(event: ReadyEvent) {
        try {
            event.client.changeUsername(defaultUserName)
            setStatus(defaultState, defaultStatus)

            MessageBuilder(event.client).apply {
                withChannel(event.client.fetchUser(getBotAdmin()).orCreatePMChannel)
                withCode("", "Ready: Initialized $shardCount shards.")
            }.build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }

    @EventSubscriber
    fun onDisconnectListener(event: DisconnectedEvent) {
        try {
            MessageBuilder(event.client).apply {
                val reason = when (event.reason) {
                    DisconnectedEvent.Reason.LOGGED_OUT -> "Logging Out"
                    DisconnectedEvent.Reason.RECONNECT_OP -> "Trying to Reconnect"
                    DisconnectedEvent.Reason.INVALID_SESSION_OP -> "Invalid Session"
                    DisconnectedEvent.Reason.ABNORMAL_CLOSE -> "Abnormal Closure"
                    else -> "null"
                }
                val message = "Shard ${event.shard.info[0]} of ${event.shard.info[1]} disconnected.\n\tReason: $reason"

                withChannel(event.client.fetchUser(getBotAdmin()).orCreatePMChannel)
                withCode("md", message)
            }.build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }

    const val defaultUserName = "MonikaBot"
    val defaultState = Status.IDLE
    const val defaultStatus = "I'm still learning (>.<)"


    fun setStatus(status: Status, playingText: String = "") {
        when (status) {
            Status.ONLINE -> client.online()
            Status.IDLE -> client.idle()
            Status.BUSY -> client.dnd()
            Status.OFFLINE -> client.invisible()
        }

        if (playingText != "") client.changePlayingText(playingText)
    }
}
