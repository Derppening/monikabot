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

    fun modifyPersistent(k: String, v: String, update: Boolean = false) {
        if (v.isBlank()) {
            persistentMap.remove(k)
        } else {
            persistentMap[k] = v
        }

        if (update) { updatePersistent() }
    }

    fun updatePersistent() {
        val s = if (persistentMap.isNotEmpty()) {
            persistentMap.map { (k, v) ->
                "$k: $v"
            }.joinToString("\n")
        } else {
            "Nothing to see here!"
        }

        Client.getMessageByID(persistentMessageId).apply {
            edit("```md\n$persistentHeader\n$s```")
        }
    }

    private fun reformat(s: String, appendString: String): String {
        val indentString = "$appendString${" ".repeat(indent - appendString.length)}"
        return "$indentString${s.replace("\n", "\n$indentString")}"
    }

    private val persistentMessageId by lazy {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("md", "$persistentHeader\nNothing to see here!")
        }.build().longID
    }

    private val persistentMap = mutableMapOf<String, String>()

    private const val persistentHeader = "[INFO]"
    private const val indent = 4
}
