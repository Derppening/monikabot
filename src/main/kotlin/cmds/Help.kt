package cmds

import core.BuilderHelper.buildEmbed
import core.Core
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder

object Help : IBase {
    override fun delegateCommand(event: MessageReceivedEvent): Parser.HandleState {
        buildEmbed(event.channel) {
            withTitle("Help Text for MonikaBot")
            withDesc("MonikaBot is a command-based bot, supporting a wide range of features. Written by " +
                    "Derppening#9062.\nSpecify `--help` to get help text specific to the command.")
            apply {
                listFunctions(this)
                if (Core.isEventFromSuperuser(event)) {
                    listSuFunctions(this)
                }
            }
            withFooterText("Package: ${this@Help.javaClass.name}")
        }

        return Parser.HandleState.HANDLED
    }

    private fun listFunctions(embed: EmbedBuilder): EmbedBuilder {
        return embed.apply {
            appendField("`echo`", "Repeats a string.", false)
            appendField("`random`", "Randomly generates numbers.", false)
            appendField("`rng`", "Computes statistics for drop tables.", false)
            appendField("`warframe`", "Warframe-related commands.", false)
        }
    }

    private fun listSuFunctions(embed: EmbedBuilder): EmbedBuilder {
        return embed.apply {
            appendField("Superuser: `clear`", "Clears all messages in a channel.", false)
            appendField("Superuser: `debug`", "Debugging commands.", false)
            appendField("Superuser: `reload`", "Reload important bot properties.", false)
            appendField("Owner: `status`", "Changes the status of the bot.", false)
            appendField("Owner: `stop`", "Terminates the bot.", false)
        }
    }
}
