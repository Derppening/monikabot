package core

import getBotAdmin
import getDebugChannel
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.MessageBuilder

private val debugChannel by lazy {
    println("Initializing Log")

    Client.getChannelByID(getDebugChannel()).apply { bulkDelete() }
            ?: Client.fetchUser(getBotAdmin()).orCreatePMChannel
}

object Log: IChannel by debugChannel {
    enum class Type {
        PLUS,
        MINUS,
        NONE
    }

    fun plus(message: String) {
        message.replace("\n", "\n+ ")

        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", "+ ${reformat(message, "+")}")
        }.build()
    }

    fun minus(message: String) {
        message.replace("\n", "\n- ")

        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", "- ${reformat(message, "-")}")
        }.build()
    }

    fun log(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", "  ${reformat(message, " ")}")
        }.build()
    }

    private fun reformat(s: String, aString: String): String {
        return s.replace("\n", "\n$aString ")
    }
}
