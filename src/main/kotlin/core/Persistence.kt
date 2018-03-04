package core

import core.Core.ownerPrivateChannel
import core.Core.privateKey
import core.Core.serverDebugChannel
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.DiscordException
import kotlin.system.exitProcess

/**
 * Singleton housing all persistent objects.
 */
object Persistence : IConsoleLogger {
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
        try {
            serverDebugChannel.apply { this?.bulkDelete() }
                    ?: ownerPrivateChannel
        } catch (e: Exception) {
            logger.error("Cannot initialize debug channel")
            e.printStackTrace()

            exitProcess(0)
        }
    }
}
