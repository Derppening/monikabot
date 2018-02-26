package core

import core.Core.adminPrivateChannel
import core.Core.privateKey
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.DiscordException

/**
 * Singleton housing all persistent objects.
 */
object Persistence {
    /**
     * Core IDiscordClient object.
     */
    val client: IDiscordClient by lazy {
        val builder = ClientBuilder().withToken(privateKey)
        try {
            builder.login()
        } catch (e: DiscordException) {
            e.printStackTrace()
            throw Exception("Unable to instantiate client")
        }
    }

    /**
     * Debug channel.
     */
    val debugChannel: IChannel by lazy {
        Client.getChannelByID(Core.debugChannel).apply { bulkDelete() }
                ?: adminPrivateChannel
    }
}
