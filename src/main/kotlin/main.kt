import cmds.Echo
import core.Client
import core.Shard
import sx.blah.discord.handle.obj.IPrivateChannel
import sx.blah.discord.util.MessageBuilder

private val adminChannel by lazy {
    Client.fetchUser(getBotAdmin()).orCreatePMChannel.bulkDelete()
    Client.fetchUser(getBotAdmin()).orCreatePMChannel
}

object Logger: IPrivateChannel by adminChannel {
    enum class Type {
        PLUS,
        MINUS,
        NONE
    }

    fun Log(type: Type = Type.NONE, message: String) {
        val msg = when (type) {
            Type.PLUS -> "+ $message"
            Type.MINUS -> "- $message"
            Type.NONE -> "  $message"
        }

        MessageBuilder(Client).apply {
            withChannel(adminChannel)
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