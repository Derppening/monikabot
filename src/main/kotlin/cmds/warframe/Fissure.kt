package cmds.warframe

import cmds.IBase
import cmds.Warframe
import core.BuilderHelper
import core.BuilderHelper.insertSeparator
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object Fissure : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        val fissures = try {
            Warframe.worldState.activeMissions
        } catch (e: NoSuchElementException) {
            BuilderHelper.buildMessage(event.channel) {
                withContent("Unable to retrieve Baro Ki'Teer information! Please try again later.")
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
                val durationToExpiry = formatTimeDuration(Duration.between(Instant.now(), it.expiry.date.numberLong))

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
                log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    /**
     * Formats a duration.
     */
    private fun formatTimeDuration(duration: Duration): String {
        return (if (duration.toDays() > 0) "${duration.toDays()}d " else "") +
                (if (duration.toHours() % 24 > 0) "${duration.toHours() % 24}h " else "") +
                (if (duration.toMinutes() % 60 > 0) "${duration.toMinutes() % 60}m " else "") +
                "${duration.seconds % 60}s"
    }
}