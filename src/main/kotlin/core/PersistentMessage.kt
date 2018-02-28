package core

import core.BuilderHelper.buildMessage
import core.Persistence.debugChannel
import sx.blah.discord.handle.obj.IChannel
import java.time.Instant
import kotlin.concurrent.thread

object PersistentMessage : IConsoleLogger, IChannel by debugChannel {
    /**
     * Modifies persistent buildMessage.
     *
     * @param header Header which the key belongs to.
     * @param key Description of the key.
     * @param value Value of the key.
     * @param doUpdate Whether to commit the updated message instantly.
     */
    fun modify(header: String, key: String, value: String, doUpdate: Boolean = false) {
        if (value.isBlank()) {
            map[header]?.remove(key)
        } else {
            if (map[header] == null) {
                map[header] = mutableMapOf(Pair(key, value)).toSortedMap()
            } else {
                map[header]?.put(key, value)
            }
        }

        map.filterValues { it.isEmpty() }.forEach { (k, _) -> map.remove(k) }

        if (doUpdate) {
            update()
        }
    }

    /**
     * Updates the persistent message in the debug channel.
     */
    private fun update() {
        logger.debug("update()")

        PersistentMessage.modify("Misc", "Last Updated", Instant.now().toString())

        val s = if (map.isNotEmpty()) {
            map.entries
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

        thread {
            Client.getMessageByID(messageId).apply {
                edit("```md\n$s```")
            }
        }
    }

    /**
     * The ID for the persistent message.
     */
    val messageId by lazy {
        buildMessage(this@PersistentMessage) {
            withCode("", "Nothing to see here!")
        }.longID
    }

    /**
     * Map of persistent information.
     */
    private val map = mutableMapOf<String, MutableMap<String, String>>()
}
