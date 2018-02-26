package cmds

import Parser
import core.Log
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

object Random : Base {
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
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withContent("Give me ${if (args.size > 2) "only " else ""}the minimum and maximum number!! >_>")
            }.build()

            return Parser.HandleState.HANDLED
        }

        val min: Double
        val max: Double
        try {
            min = args[0].toDoubleOrNull() ?: throw Exception("Minimum number is not a number!")
            max = args[1].toDoubleOrNull() ?: throw Exception("Maximum number is not a number!")

            if (min >= max) throw Exception("Minimum number is bigger than the maximum!")
        } catch (e: Exception) {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withContent("${e.message} >_>")
            }.build()

            return Parser.HandleState.HANDLED
        }

        MessageBuilder(event.client).apply {
            withChannel(event.channel)
            if (isReal) {
                val n = generateReal(min, max)
                withContent("You got $n!")
            } else {
                val n = generateInt(min.toInt(), (max + 1).toInt())
                withContent("You got a $n!")
            }
        }.build()

        return Parser.HandleState.HANDLED
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withCode("", "Usage: random [min] [max]\n" +
                        "Random: Randomizes a number, with range of [min] to [max] (inclusive).\n\n" +
                        "You can also \"random coin\" or \"random dice\"!")
            }.build()
        } catch (e: DiscordException) {
            Log.minus(javaClass.name,
                    "Cannot display help text",
                    null,
                    event.author,
                    event.channel,
                    e.errorMessage)
            e.printStackTrace()
        }
    }

    private fun rollDie(event: MessageReceivedEvent) {
        MessageBuilder(event.client).apply {
            withChannel(event.channel)
            val n = generateInt(1, 7)
            withContent("You got a $n!")
        }.build()
    }

    private fun flipCoin(event: MessageReceivedEvent) {
        MessageBuilder(event.client).apply {
            withChannel(event.channel)
            val n = generateInt(0, 2)
            withContent("You got ${if (n == 0) "tails" else "heads"}!")
        }.build()
    }

    private fun generateInt(min: Int, max: Int): Int = java.util.Random().nextInt(max - min) + min
    private fun generateReal(min: Double, max: Double): Double = (java.util.Random().nextDouble() * (max - min)) + min
}