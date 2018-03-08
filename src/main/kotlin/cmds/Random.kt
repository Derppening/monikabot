package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.Core
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Random : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Core.getArgumentList(event.message.content).toMutableList()

        // special cases
        if (args.size == 1 && args[0].matches(Regex("dic?e"))) {
            rollDie(event)
            return Parser.HandleState.HANDLED
        } else if (args.size == 1 && args[0] == "coin") {
            flipCoin(event)
            return Parser.HandleState.HANDLED
        }

        val isReal = args.contains("real").also { args.removeIf { it.contains("real") } }
        args.remove("from")
        args.remove("to")

        if (args.size != 2) {
            buildMessage(event.channel) {
                withContent("Give me ${if (args.size > 2) "only " else ""}the minimum and maximum number!! >_>")
            }

            return Parser.HandleState.HANDLED
        }

        val min: Double
        val max: Double
        try {
            min = args[0].toDoubleOrNull() ?: throw Exception("Minimum number is not a number!")
            max = args[1].toDoubleOrNull() ?: throw Exception("Maximum number is not a number!")

            if (min >= max) throw Exception("Minimum number is bigger than the maximum!")
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent("${e.message} >_>")
            }

            return Parser.HandleState.HANDLED
        }

        buildMessage(event.channel) {
            if (isReal) {
                val n = generateReal(min, max)
                withContent("You got $n!")
            } else {
                val n = generateInt(min.toInt(), (max + 1).toInt())
                withContent("You got a $n!")
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `random`")
                withDesc("Randomly generates numbers. Also works for dices and coins.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```random [real] [min] [max]```", false)
                appendField("`real`", "If specified, generate a real number instead of an integer.", false)
                appendField("`[min] [max]`", "Specify the minimum and maximum numbers (inclusive) to generate.", false)
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```random [coin|dice]```", false)
                appendField("`[coin|dice]`", "Special modes to generate output based on a coin/dice.", false)
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }

    private fun rollDie(event: MessageReceivedEvent) {
        buildMessage(event.channel) {
            withContent("You got a ${generateInt(1, 7)}!")
        }
    }

    private fun flipCoin(event: MessageReceivedEvent) {
        buildMessage(event.channel) {
            withContent("You got ${if (generateInt(0, 2) == 0) "tails" else "heads"}!")
        }
    }

    private fun generateInt(min: Int, max: Int): Int = java.util.Random().nextInt(max - min) + min
    private fun generateReal(min: Double, max: Double): Double = (java.util.Random().nextDouble() * (max - min)) + min
}