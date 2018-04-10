/*
 *  This file is part of MonikaBot.
 *
 *  Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 *  MonikaBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MonikaBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.pow

object RNG : IBase {
    enum class Rounding {
        DECIMAL_PLACES,
        SIGNIFICANT_FIGURES
    }

    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        var prob = Pair(false, 0.0)
        var attempts = Pair(false, 0)
        var success = Pair(false, 0)
        var round = Pair(Rounding.DECIMAL_PLACES, 3)

        try {
            args.forEach {
                when {
                    it.startsWith("p") -> prob = Pair(true, it.substring(it.indexOf('=') + 1).toDouble())
                    it.startsWith("n") -> attempts = Pair(true, it.substring(it.indexOf('=') + 1).toInt())
                    it.startsWith("k") -> success = Pair(true, it.substring(it.indexOf('=') + 1).toInt())
                    it.startsWith("r") -> {
                        round = when {
                            it.endsWith("dp") -> Pair(Rounding.DECIMAL_PLACES, it.substring(it.indexOf('=') + 1, it.indexOf("dp")).toInt())
                            it.endsWith("sf") -> Pair(Rounding.SIGNIFICANT_FIGURES, it.substring(it.indexOf('=') + 1, it.indexOf("sf")).toInt())
                            else -> error("Specifies rounding does not make sense!")
                        }
                    }
                }
            }

            if (!prob.first) {
                error("I need the probability!")
            } else if (prob.second <= 0.0 || prob.second > 1.0) {
                error("Probability must be between 0.0 and 1.0!")
            } else if (attempts.second < 0 || success.second < 0) {
                error("You cannot try or succeed a negative amount of times!")
            } else if (attempts.second < success.second) {
                error("You can't succeed more times than you tried!")
            } else if (round.first == Rounding.DECIMAL_PLACES && round.second < 0) {
                error("You cannot format a number to a negative number of decimal places!")
            } else if (round.first == Rounding.SIGNIFICANT_FIGURES && round.second <= 0) {
                error("You cannot format a number to a negative number of significant figures!")
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
            withDesc("With probability=$p, " +
                    "attempts=${if (attempts.first) n.toString() else "(not given)"}, " +
                    "successes=${if (success.first) k.toString() else "(not given)"}")

            appendField("Mean Attempts for First Success", formatReal(1 / p, round), true)
            appendField("Variance for First Success", formatReal((1 - p) / p.pow(2), round), true)

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
                insertSeparator()
                appendField("Mean of Successes", formatReal(n * p, round), true)
                appendField("Variance of Successes", formatReal(n * p * (1 - p), round), true)
                appendField("Chance of Failure after $n Attempts", formatReal((1 - p).pow(n), round, true), true)
            }

            if (success.first) {
                insertSeparator()
                appendField("Chance of Success during Run $k", formatReal((1 - p).pow(k - 1) * p, round, true), true)

                val percentile = run {
                    (1..k).sumByDouble { ((1 - p).pow(it - 1) * p) }
                }

                appendField("Percentile", formatReal(percentile, round, true), true)
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `rng`")
            withDesc("Computes distribution statistics for drop tables.")
            insertSeparator()
            appendField("Usage", "```rng p=[PROBABILITY] [n=ATTEMPTS] [k=SUCCESS_TRIAL] [r=ROUND]```", false)
            appendField("`[PROBABILITY]`", "Specifies item drop chance.", false)
            appendField("`[n=ATTEMPTS]`", "Optional: Specifies number of attempts to get the item.", false)
            appendField("`[k=SUCCESSFUL_TRIAL]`", "Optional: Specifies the number of trial which you got the item.", false)
            appendField("`[r=ROUND]`", "Optional: Specifies rounding. You may use dp to signify decimal places and " +
                    "sf to signify significant figures. \nDefaults to 3 decimal places.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }

    }

    /**
     * Formats a real number.
     *
     * @param double Number to format.
     * @param rounding Type of rounding to use.
     *
     * @return String of formatted number.
     */
    private fun formatReal(double: Double, rounding: Pair<Rounding, Int>, isPercent: Boolean = false): String {
        return when (rounding.first) {
            Rounding.DECIMAL_PLACES -> formatRealDecimal(double, rounding.second, isPercent)
            Rounding.SIGNIFICANT_FIGURES -> formatRealSigFig(double, rounding.second, isPercent)
        }
    }

    /**
     * Formats a real number by decimal places.
     *
     * @param double Number to format.
     * @param dp Decimal places.
     * @param isPercent whether to format as a percentage.
     */
    private fun formatRealDecimal(double: Double, dp: Int, isPercent: Boolean): String {
        return if (isPercent) {
            "%.${dp}f%%".format(double * 100)
        } else {
            "%.${dp}f".format(double)
        }
    }

    /**
     * Formats a real number by significant figures.
     *
     * @param double Number to format.
     * @param sf Significant figures.
     * @param isPercent whether to format as a percentage.
     */
    private fun formatRealSigFig(double: Double, sf: Int, isPercent: Boolean): String {
        return if (isPercent) {
            "${BigDecimal(double * 100).round(MathContext(sf)).toDouble()}%"
        } else {
            BigDecimal(double).round(MathContext(sf)).toDouble().toString()
        }
    }
}