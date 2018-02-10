import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.obj.Message
import sx.blah.discord.handle.impl.obj.User
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

class Client {
    enum class Status {
        ONLINE,
        IDLE,
        BUSY,
        OFFLINE
    }

    @EventSubscriber
    fun onReadyListener(event: ReadyEvent) {
        println("DEBUG: onReadyListener()")
        try {
            event.client.changeUsername("MonikaBot")
            setStatus(Status.IDLE, "I'm still learning (>.<)")

            MessageBuilder(event.client)
                    .withChannel(event.client.shards[0].fetchUser(getBotAdmin()).orCreatePMChannel)
                    .withContent("Hii I'm alive!")
                    .build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }

    fun setStatus(status: Status, playingText: String = "") {
        when (status) {
            Status.ONLINE -> Instance.client.online()
            Status.IDLE -> Instance.client.idle()
            Status.BUSY -> Instance.client.dnd()
            Status.OFFLINE -> Instance.client.invisible()
        }

        if (playingText != "") Instance.client.changePlayingText(playingText)
    }

    object Instance {
        val client = createClient(getPrivateKey())
        val dispatcher = client.dispatcher ?: throw Exception("Unable to get Event Dispatcher")

        private fun createClient(token: String): IDiscordClient {
            val builder = ClientBuilder().withToken(token)
            try {
                return builder.login()
            } catch (e: DiscordException) {
                e.printStackTrace()
                throw Exception("Unable to instantiate Client")
            }
        }
    }
}