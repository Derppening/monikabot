package core

import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.DiscordException

object Persistence {
    val client: IDiscordClient by lazy {
        val builder = ClientBuilder().withToken(Core.getPrivateKey())
        try {
            builder.login()
        } catch (e: DiscordException) {
            e.printStackTrace()
            throw Exception("Unable to instantiate client")
        }
    }

    val debugChannel by lazy {
        Client.getChannelByID(Core.getDebugChannel()).apply { bulkDelete() }
                ?: Core.getAdminPrivateChannel()
    }
}
