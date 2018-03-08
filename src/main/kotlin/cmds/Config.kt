package cmds

import core.BuilderHelper
import core.BuilderHelper.buildMessage
import core.Core
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Config : IBase {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content)

        if (args.isEmpty()) {
            Config.help(event, true)
            return Parser.HandleState.HANDLED
        }

        when (args[0]) {
            "experimental" -> {
                if (args.size != 2) {
                    BuilderHelper.buildEmbed(event.channel) {
                        withTitle("Help Text for config-experimental`")
                        withDesc("Whether to enable experimental features.")
                        appendField("\u200B", "\u200B", false)
                        appendField("Usage", "```config experimental [enable|disable]```", false)
                        appendField("`[enable|disable]`", "Whether to enable experimental features.", false)
                        withFooterText("Package: ${this@Config.javaClass.name}")
                    }
                }

                enableExperimentalFeatures = args[1].toBoolean() || args[1] == "enable"
                buildMessage(event.channel) {
                    withContent("Experimental Features is now ${if (enableExperimentalFeatures) "enabled" else "disabled"}.")
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    var enableExperimentalFeatures = false
        private set
}