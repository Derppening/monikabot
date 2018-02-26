package core

import IConsoleLogger
import core.Core.getChannelName
import core.Core.getDiscordTag
import core.Persistence.debugChannel
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.MessageBuilder
import kotlin.concurrent.thread

object Log : IConsoleLogger, IChannel by debugChannel {
    /**
     * Logs a message to the debug channel in green.
     *
     * @param className Name of the invocation class. Should always be javaClass.name.
     * @param message Logging message.
     * @param srcMessage Full text of message causing this invocation, if any.
     * @param srcAuthor Owner of message causing this invocation, if any.
     * @param srcChannel Channel of message causing this invocation, if any.
     * @param info Additional information.
     */
    fun plus(className: String,
             message: String,
             srcMessage: IMessage? = null,
             srcAuthor: IUser? = null,
             srcChannel: IChannel? = null,
             info: String = "") {
        plus("$className: $message" +
                (if (srcMessage == null) "" else "\n\tCaused by message \"${srcMessage.content}\"") +
                (if (srcAuthor == null) "" else "\n\tFrom ${getDiscordTag(srcAuthor)}") +
                (if (srcChannel == null) "" else "\n\tIn \"${getChannelName(srcChannel)}\"") +
                if (info.isBlank()) "" else "\n\tInfo: $info")
    }

    /**
     * Logs a message to the debug channel in red.
     *
     * @param className Name of the invocation class. Should always be javaClass.name.
     * @param message Logging message.
     * @param srcMessage Full text of message causing this invocation, if any.
     * @param srcAuthor Owner of message causing this invocation, if any.
     * @param srcChannel Channel of message causing this invocation, if any.
     * @param reason Additional reason.
     */
    fun minus(className: String,
              message: String,
              srcMessage: IMessage? = null,
              srcAuthor: IUser? = null,
              srcChannel: IChannel? = null,
              reason: String = "") {
        minus("$className: $message" +
                (if (srcMessage == null) "" else "\n\tCaused by message \"${srcMessage.content}\"") +
                (if (srcAuthor == null) "" else "\n\tFrom ${getDiscordTag(srcAuthor)}") +
                (if (srcChannel == null) "" else "\n\tIn \"${getChannelName(srcChannel)}\"") +
                if (reason.isBlank()) "" else "\n\tReason: $reason")
    }

    /**
     * Logs a message to the debug channel in grey.
     *
     * @param className Name of the invocation class. Should always be javaClass.name.
     * @param message Logging message.
     * @param srcMessage Full text of message causing this invocation, if any.
     * @param srcAuthor Owner of message causing this invocation, if any.
     * @param srcChannel Channel of message causing this invocation, if any.
     * @param info Additional information.
     */
    fun log(className: String,
            message: String,
            srcMessage: IMessage? = null,
            srcAuthor: IUser? = null,
            srcChannel: IChannel? = null,
            info: String = "") {
        log("$className: $message" +
                (if (srcMessage == null) "" else "\n\tCaused by message \"${srcMessage.content}\"") +
                (if (srcAuthor == null) "" else "\n\tFrom ${getDiscordTag(srcAuthor)}") +
                (if (srcChannel == null) "" else "\n\tIn \"${getChannelName(srcChannel)}\"") +
                if (info.isBlank()) "" else "\n\tInfo: $info")
    }

    /**
     * Modifies persistent message.
     *
     * @param header Header which the key belongs to.
     * @param key Description of the key.
     * @param value Value of the key.
     * @param doUpdate Whether to commit the updated message instantly.
     */
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

        persistentMap.filterValues { it.isEmpty() }.forEach { (k, _) -> persistentMap.remove(k) }

        if (doUpdate) {
            updatePersistent()
        }
    }

    /**
     * Updates the persistent message in the debug channel.
     */
    fun updatePersistent() {
        logger.debug("updatePersistent()")

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

        thread {
            Client.getMessageByID(persistentMessageId).apply {
                edit("```md\n$s```")
            }
        }
    }

    /**
     * Logs a message to the debug channel in green.
     *
     * @param message The message itself.
     */
    private fun plus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, "+"))
        }.build()
    }

    /**
     * Logs a message to the debug channel in red.
     *
     * @param message The message itself.
     */
    private fun minus(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, "-"))
        }.build()
    }

    /**
     * Logs a message to the debug channel in grey.
     */
    private fun log(message: String) {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("diff", reformat(message, " "))
        }.build()
    }

    /**
     * Reformats a string such that all lines start with [appendString].
     *
     * @param s Input string.
     * @param appendString String to be appended.
     */
    private fun reformat(s: String, appendString: String): String {
        val indentString = "$appendString${" ".repeat(indent - appendString.length)}"
        return "$indentString${s.replace("\n", "\n$indentString")}"
    }

    /**
     * The ID for the persistent message.
     */
    val persistentMessageId by lazy {
        MessageBuilder(Client).apply {
            withChannel(this@Log)
            withCode("md", "Nothing to see here!")
        }.build().longID
    }

    override val logger = LoggerFactory.getLogger(this::class.java)!!

    /**
     * Map of persistent information.
     */
    private val persistentMap = mutableMapOf<String, MutableMap<String, String>>()

    /**
     * Fixed indentation for reformat.
     */
    private const val indent = 4
}
