package core

import core.Core.getChannelName
import core.Core.getDiscordTag
import core.Persistence.debugChannel
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.MessageBuilder
import java.util.*

object Log : IChannel by debugChannel {
    fun plus(className: String,
             message: String,
             srcAuthor: IUser? = null,
             srcChannel: IChannel? = null,
             info: String = "") {
        plus("$className: $message" +
                (if (srcAuthor == null) "" else "\n\tInvoked by ${getDiscordTag(srcAuthor)}") +
                (if (srcChannel == null) "" else "\n\tIn \"${getChannelName(srcChannel)}\"") +
                if (info.isBlank()) "" else "\n\tInfo: $info")
    }

    fun minus(className: String,
              message: String,
              srcAuthor: IUser? = null,
              srcChannel: IChannel? = null,
              reason: String = "") {
        minus("$className: $message" +
                (if (srcAuthor == null) "" else "\n\tInvoked by ${getDiscordTag(srcAuthor)}") +
                (if (srcChannel == null) "" else "\n\tIn \"${getChannelName(srcChannel)}\"") +
                if (reason.isBlank()) "" else "\n\tReason: $reason")
    }

    fun log(className: String,
            message: String,
            srcAuthor: IUser? = null,
            srcChannel: IChannel? = null,
            info: String = "") {
        log("$className: $message" +
                (if (srcAuthor == null) "" else "\n\tInvoked by ${getDiscordTag(srcAuthor)}") +
                (if (srcChannel == null) "" else "\n\tIn \"${getChannelName(srcChannel)}\"") +
                if (info.isBlank()) "" else "\n\tInfo: $info")
    }

    fun modifyPersistent(header: String, key: String, value: String, doUpdate: Boolean = false) {
        if (value.isBlank()) {
            persistentMap[header]?.remove(key)
        } else {
            if (persistentMap[header] == null) {
                persistentMap[header] = mutableMapOf(Pair(key, value)).toSortedMap()
            } else {
                persistentMap[header]?.put(key, value)
            }
        }

        if (doUpdate) {
            updatePersistent()
        }
    }

    fun updatePersistent() {
        val s = if (persistentMap.isNotEmpty()) {
            persistentMap.entries
                    .sortedWith(compareBy({ it.key == "Misc" }, { it.key }))
                    .joinToString("\n\n") { (h, p) ->
                        val pairsInHeader = p.entries.joinToString("\n") { (k, v) ->
                            "$k: $v"
                        }
                        "[$h]\n$pairsInHeader"
                    }
        } else {
            "Nothing to see here!"
        }

        Client.getMessageByID(persistentMessageId).apply {
            edit("```md\n$s```")
        }
    }

    private fun plus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, "+"))
        }.build()
    }

    private fun minus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, "-"))
        }.build()
    }

    private fun log(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, " "))
        }.build()
    }

    private fun reformat(s: String, appendString: String): String {
        val indentString = "$appendString${" ".repeat(indent - appendString.length)}"
        return "$indentString${s.replace("\n", "\n$indentString")}"
    }

    private val persistentMessageId by lazy {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("md", "Nothing to see here!")
        }.build().longID
    }

    private val persistentMap = sortedMapOf<String, SortedMap<String, String>>()

    private const val indent = 4
}
