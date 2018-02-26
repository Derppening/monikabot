package core

import core.Core.adminPrivateChannel
import core.Core.privateKey
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.DiscordException

object Persistence {
    val client: IDiscordClient by lazy {
        val builder = ClientBuilder().withToken(privateKey)
        try {
            builder.login()
        } catch (e: DiscordException) {
            e.printStackTrace()
            throw Exception("Unable to instantiate client")
        }
    }

    val debugChannel: IChannel by lazy {
        Client.getChannelByID(Core.debugChannel).apply { bulkDelete() }
                ?: adminPrivateChannel
    }
}
