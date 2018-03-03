package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.IChannelLogger
import core.Parser
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import kotlin.math.pow

object RNG : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = Parser.popLeadingMention(event.message.content).popFirstWord().split(" ")

        var prob = Pair(false, 0.0)
        var attempts = Pair(false, 0)
        var success = Pair(false, 0)
        var round = Pair('d', 3)

        try {
            args.forEach {
                when {
                    it.startsWith("p") -> prob = Pair(true, it.substring(it.indexOf('=') + 1).toDouble())
                    it.startsWith("n") -> attempts = Pair(true, it.substring(it.indexOf('=') + 1).toInt())
                    it.startsWith("k") -> success = Pair(true, it.substring(it.indexOf('=') + 1).toInt())
                    it.startsWith("r") -> {
                        round = if (it.endsWith("dp")) {
                            Pair('d', it.substring(it.indexOf('=') + 1, it.indexOf("dp")).toInt())
//                        } else if (it.endsWith("sf")) {
//                            Pair('s', it.substring(it.indexOf('=') + 1, it.indexOf("sf")).toInt())
                        } else {
                            throw Exception("Specifies rounding does not make sense!")
                        }
                    }
                }
            }

            if (!prob.first) {
                throw Exception("I need the probability")
            } else if (prob.second < 0.0 || prob.second > 1.0) {
                throw Exception("Probability must be between 0.0 and 1.0!")
            } else if (attempts.second < success.second) {
                throw Exception("You can't succeed more times than you tried!")
            } else if (attempts.second < 0 || success.second < 0) {
                throw Exception("You cannot try or succeed a negative amount of times!")
            }
        } catch (e: NumberFormatException) {
            buildMessage(event.channel) {
                withContent("One of the numbers is not formatted properly!")
            }

            return Parser.HandleState.HANDLED
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent(e.message)
            }

            return Parser.HandleState.HANDLED
        }

        buildEmbed(event.channel) {
            val p = prob.second
            val n = attempts.second
            val k = success.second

            withTitle("Probability Calculations")
            withDesc("With probability=${prob.second}, " +
                    "attempts=${if (attempts.first) n.toString() else "(not given)"}, " +
                    "successes=${if (success.first) k.toString() else "(not given)"}")

            appendField("Mean Attempts for First Success", formatReal(1 / p, round.second), true)
            appendField("Variance for First Success", formatReal((1 - p) / p.pow(2), round.second), true)

            val min = run {
                var min50 = 0
                val min90: Int
                var i = 0
                while (true) {
                    if (min50 == 0 && 1 - (1 - p).pow(i) > 0.5) {
                        min50 = i
                    }
                    if (1 - (1 - p).pow(i) > 0.9) {
                        min90 = i
                        break
                    }
                    ++i
                }

                Pair(min50, min90)
            }
            appendField("Number of Attempts for >50% Chance", min.first.toString(), true)
            appendField("Number of Attempts for >90% Chance", min.second.toString(), true)

            if (attempts.first) {
                appendField("\u200B", "\u200B", false)
                appendField("Mean of Successes", formatReal(n * p, round.second), true)
                appendField("Variance of Successes", formatReal(n * p * (1 - p), round.second), true)
                appendField("Chance of Failure after $n Attempts", formatReal((1 - p).pow(n), round.second, true), true)
            }

            if (success.first) {
                appendField("\u200B", "\u200B", false)
                appendField("Chance of Success during Run $k", formatReal((1 - p).pow(k - 1) * p, round.second, true), true)

                val percentile = run {
                    val c = (1..k).sumByDouble { ((1 - p).pow(it - 1) * p) }
                    c
                }

                appendField("Percentile", formatReal(percentile, round.second, true), true)
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `rng`")
                withDesc("Computes distribution statistics for drop tables.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```rng p=[PROBABILITY] [n=ATTEMPTS] [k=SUCCESS_TRIAL] [r=ROUND]```", false)
                appendField("`[PROBABILITY]`", "Specifies item drop chance.", false)
                appendField("`[n=ATTEMPTS]`", "Optional: Specifies number of attempts to get the item.", false)
                appendField("`[k=SUCCESSFUL_TRIAL]`", "Optional: Specifies the number of trial which you got the item.", false)
                appendField("`[r=ROUND]`", "Optional: Specifies the rounding. Defaults to 3 decimal places.", false)
                withFooterText("Package: ${this@RNG.javaClass.name}")
            }
        } catch (e: DiscordException) {
            Random.log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }

    private fun formatReal(double: Double, dp: Int = 2, isPercent: Boolean = false): String {
        return if (isPercent) {
            "%.${dp}f%%".format(double * 100)
        } else {
            "%.${dp}f".format(double)
        }
    }
}