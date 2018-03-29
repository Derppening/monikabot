package cmds.warframe

import cmds.IBase
import cmds.Warframe
import cmds.Warframe.formatDuration
import core.BuilderHelper
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object Fissure : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        val fissures = try {
            Warframe.worldState.activeMissions
        } catch (e: NoSuchElementException) {
            BuilderHelper.buildMessage(event.channel) {
                withContent("Unable to retrieve fissure missions! Please try again later.")
            }

            return Parser.HandleState.HANDLED
        }

        event.channel.toggleTypingStatus()
        BuilderHelper.buildEmbed(event.channel) {
            withTitle("Ongoing Fissures")

            fissures.sortedBy { it.modifier }.forEach {
                val nodeName = WorldState.getSolNode(it.node).value
                val missionType = WorldState.getMissionType(it.missionType)
                val tier = WorldState.getFissureModifier(it.modifier)
                val durationToExpiry = Duration.between(Instant.now(), it.expiry.date.numberLong).formatDuration()

                appendField("$tier $missionType on $nodeName", "Time Left: $durationToExpiry", false)
            }

            withTimestamp(Instant.now())
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        BuilderHelper.buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-fissure`")
            withDesc("Displays ongoing fissure missions.")
            insertSeparator()
            appendField("Usage", "```warframe fissures```", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }
}