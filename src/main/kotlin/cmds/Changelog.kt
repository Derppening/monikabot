package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.Core
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.io.File

object Changelog : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content)

        val changes = readChangelog()

        val showRelease = args.any { it.matches(Regex("-{0,2}release")) }
        val showAllChanges = args.any { it.matches(Regex("-{0,2}all")) }

        if (showAllChanges) {
            outputAllChanges(event, changes, showRelease)
        } else {
            outputLatestChanges(event, changes, showRelease)
        }


        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `changelog`")
                withDesc("Displays the changelog for the most recent build(s).")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```changelog [release] [all]```", false)
                appendField("`release`", "Only show changes for release builds.", false)
                appendField("`all`", "Show 5 most recent builds instead of 1.", false)
                withFooterText("Package: ${this@Changelog.javaClass.name}")
            }
        } catch (e: DiscordException) {
            Clear.log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }

    private fun outputAllChanges(event: MessageReceivedEvent, changes: List<Pair<String, List<String>>>, showRel: Boolean) {
        val displayChanges = if (showRel) {
            changes.filterNot { (k, _) -> k.contains('-') }
        } else {
            changes
        }

        buildEmbed(event.channel) {
            withTitle("Last 5 Changelogs")
            if (displayChanges.isEmpty()) {
                withDesc("There are no official releases (yet)!")
            } else {
                displayChanges.filterIndexed { index, _ -> index >= changes.size - 5 }
                        .forEach { (ver, changetext) ->
                            appendField(ver, changetext.joinToString("\n"), false)
                        }
            }
            withFooterText("Package: ${this@Changelog.javaClass.name}")
        }
    }

    private fun outputLatestChanges(event: MessageReceivedEvent, changes: List<Pair<String, List<String>>>, showRel: Boolean) {
        val displayChange: Pair<String, List<String>>

        try {
            displayChange = if (showRel) {
                changes.filterNot { (k, _) -> k.contains('-') }
            } else {
                changes
            }.last()
        } catch (nsee: NoSuchElementException) {
            buildMessage(event.channel) {
                withContent("There are no official releases (yet)!")
            }

            return
        }

        buildEmbed(event.channel) {
            withTitle("Changelog for ${displayChange.first}")
            withDesc(displayChange.second.joinToString("\n"))
            withFooterText("Package: ${this@Changelog.javaClass.name}")
        }

    }

    private fun readChangelog(): List<Pair<String, List<String>>> {
        val contents = File(Thread.currentThread().contextClassLoader.getResource("lang/Changelog.md").toURI()).readLines()

        val logMap = mutableMapOf<String, MutableList<String>>()
        var ver = ""
        for (line in contents) {
            if (line.startsWith('[') && line.endsWith(']')) {
                ver = line.substring(1, line.lastIndex)
                logMap[ver] = mutableListOf()
            } else if (ver.isNotBlank()) {
                logMap[ver]?.add(line)
            }
        }

        return logMap.toList()
    }
}