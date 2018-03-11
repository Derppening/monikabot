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
                    "Derppening#9062.\nUse `[command] --help` to get help text for any command listed below.")
            appendDesc("\nCommands listed as *experimental* can be accessed by appending `--experimental` to the " +
                    "command itself, but note that these commands are subject to change and may not be stable.")
            apply {
                listFunctions(this)
                if (Core.isEventFromSuperuser(event)) {
                    listSuFunctions(this)
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    private fun listFunctions(embed: EmbedBuilder): EmbedBuilder {
        return embed.apply {
            appendField("`changelog`", "Views the changelog of MonikaBot.", false)
            appendField("`echo`", "Repeats a string.", false)
            appendField("`help`", "Displays help for primary commands.", false)
            appendField("`random`", "Randomly generates numbers.", false)
            appendField("`rng`", "Computes statistics for drop tables.", false)
            appendField("`version`", "Displays the current version of MonikaBot.", false)
            appendField("`warframe`", "Warframe-related commands.", false)
            appendField("Experimental: `trivia`", "Starts a game of trivia.", false)
        }
    }

    private fun listSuFunctions(embed: EmbedBuilder): EmbedBuilder {
        return embed.apply {
            appendField("Superuser: `clear`", "Clears all messages in a channel.", false)
            appendField("Superuser: `config`", "Configures properties of MonikaBot.", false)
            appendField("Superuser: `debug`", "Debugging commands.", false)
            appendField("Superuser: `reload`", "Reload important bot properties.", false)
            appendField("Owner: `status`", "Changes the status of the bot.", false)
            appendField("Owner: `stop`", "Terminates the bot.", false)
        }
    }
}
