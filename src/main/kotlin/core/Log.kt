package core

import core.Persistence.debugChannel
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.MessageBuilder
import java.util.*

object Log: IChannel by debugChannel {
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

        if (doUpdate) { updatePersistent() }
    }

    fun updatePersistent() {
        val s = if (persistentMap.isNotEmpty()) {
            persistentMap.entries
                    .sortedWith(compareBy( { it.key == "Misc" }, { it.key }))
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
