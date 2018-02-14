import cmds.Echo
import core.Client
import core.Shard
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.MessageBuilder

private val debugChannel by lazy {
    Client.getChannelByID(getDebugChannel())
}

object Logger: IChannel by debugChannel {
    enum class Type {
        PLUS,
        MINUS,
        NONE
    }

    fun log(type: Type = Type.NONE, message: String) {
        val msg = when (type) {
            Type.PLUS -> "+ $message"
            Type.MINUS -> "- $message"
            Type.NONE -> "  $message"
        }

        MessageBuilder(Client).apply {
            withChannel(debugChannel)
            withCode("diff", msg)
        }.build()
    }
}

fun setupDispatchers() {
    // core
    Client.dispatcher.registerListener(Client)
    Client.dispatcher.registerListener(Shard())

    // cmds
    Client.dispatcher.registerListener(Echo())
}

fun main(args: Array<String>) {
    setupDispatchers()
}