package core

import getBotAdmin
import getPrivateKey
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

private val client by lazy {
    val builder = ClientBuilder().withToken(getPrivateKey())
    try {
        builder.login()
    } catch (e: DiscordException) {
        e.printStackTrace()
        throw Exception("Unable to instantiate core.Client")
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
                val withChannel = withChannel(event.client.fetchUser(getBotAdmin()).orCreatePMChannel)
                withCode("diff", "+ Client Ready: Initialized $shardCount shard(s).")
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
