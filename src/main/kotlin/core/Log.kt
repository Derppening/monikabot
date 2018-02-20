package core

import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.MessageBuilder

private val debugChannel by lazy {
    println("Initializing Log")

    Client.getChannelByID(Core.getDebugChannel()).apply { bulkDelete() }
            ?: Core.getAdminPrivateChannel()
}

object Log: IChannel by debugChannel {
    enum class Type {
        PLUS,
        MINUS,
        NONE
    }

    fun plus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, "+"))
        }.build()
    }

    fun minus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, "-"))
        }.build()
    }

    fun log(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, " "))
        }.build()
    }

    private fun reformat(s: String, appendString: String): String {
        val indentString = "$appendString${" ".repeat(indent - appendString.length)}"
        return "$indentString${s.replace("\n", "\n$indentString")}"
    }

    private const val indent = 4
}
