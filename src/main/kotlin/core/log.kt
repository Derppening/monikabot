package core

import getDebugChannel
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.MessageBuilder

private val debugChannel by lazy {
    println("Initializing Log")
    Client.getChannelByID(getDebugChannel()).bulkDelete()
    Client.getChannelByID(getDebugChannel())
}

object Log: IChannel by debugChannel {
    enum class Type {
        PLUS,
        MINUS,
        NONE
    }

    fun plus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(debugChannel)
            withCode("diff", "+ $message")
        }.build()
    }

    fun minus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(debugChannel)
            withCode("diff", "- $message")
        }.build()
    }

    fun log(message: String) {
        MessageBuilder(Client).apply {
            withChannel(debugChannel)
            withCode("diff", "  $message")
        }.build()
    }
}
