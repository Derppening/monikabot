package cmds

import Parser
import core.BuilderHelper.buildMessage
import core.IChannelLogger
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Random : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Parser.popLeadingMention(event.message.content).popFirstWord().split(" ").toMutableList()

        // special cases
        if (args[0].matches("-{0,2}help".toRegex())) {
            help(event, false)
            return Parser.HandleState.HANDLED
        } else if (args.size == 1 && args[0].matches("dic?e".toRegex())) {
            rollDie(event)
            return Parser.HandleState.HANDLED
        } else if (args.size == 1 && args[0] == "coin") {
            flipCoin(event)
            return Parser.HandleState.HANDLED
        }

        val isReal = args.contains("real").also { args.remove("real") }
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

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildMessage(event.channel) {
                withCode("", "Usage: random [min] [max]\n" +
                        "       random [coin|dice]" +
                        "Random: Randomizes a number, with range of [min] to [max] (inclusive).\n\n" +
                        "Using \"coin\"/\"dice\" as the argument does what you expect it to do.")
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