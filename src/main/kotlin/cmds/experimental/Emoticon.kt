package cmds.experimental

import cmds.IBase
import core.BuilderHelper.buildMessage
import core.Core.popLeadingMention
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.io.File
import java.nio.file.Paths

object Emoticon : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val testRegex = popLeadingMention(event.message.content, event.guild)
                .toLowerCase().replace("*", ".+").dropLastWhile { it == '!' }
        val matching = pairs.filter { it.key.matches(testRegex.toRegex()) }

        return when (matching.size) {
            0 -> {
                Parser.HandleState.NOT_FOUND
            }
            1 -> {
                buildMessage(event.channel) {
                    withContent(matching.values.first())
                }
                Parser.HandleState.HANDLED
            }
            else -> {
                buildMessage(event.author.orCreatePMChannel) {
                    withContent("Multiple Matches!\n\n")
                    appendContent(matching.entries.joinToString("\n") { "${it.key} - ${it.value}" })
                }
                Parser.HandleState.UNHANDLED
            }
        }
    }

    private fun readFromFile(): Map<String, String> {
        val file = File(Paths.get("persistent/emoticons.txt").toUri())
        val contents = file.readLines()
        val m = mutableMapOf<String, String>()

        contents.forEach {
            val key = it.split("=").first()
            val value = it.split("=").drop(1).joinToString("=")
            m[key] = value
        }

        return m
    }

    private val pairs = readFromFile()
}