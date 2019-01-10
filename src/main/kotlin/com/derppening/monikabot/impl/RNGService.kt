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

package com.derppening.monikabot.impl

import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.helpers.NumericHelper
import com.derppening.monikabot.util.helpers.NumericHelper.formatReal
import com.derppening.monikabot.util.helpers.insertSeparator
import sx.blah.discord.util.EmbedBuilder
import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.pow

object RNGService : ILogger {
    fun computeRNGStats(args: List<String>): Result {
        var prob = Pair(false, 0.0)
        var attempts = Pair(false, 0)
        var success = Pair(false, 0)
        var round = Pair(NumericHelper.Rounding.DECIMAL_PLACES, 3)

        try {
            args.forEach {
                when {
                    it.startsWith("p") -> prob = Pair(true, it.substring(it.indexOf('=') + 1).toDouble())
                    it.startsWith("n") -> attempts = Pair(true, it.substring(it.indexOf('=') + 1).toInt())
                    it.startsWith("k") -> success = Pair(true, it.substring(it.indexOf('=') + 1).toInt())
                    it.startsWith("r") -> {
                        round = when {
                            it.endsWith("dp") -> Pair(NumericHelper.Rounding.DECIMAL_PLACES, it.substring(it.indexOf('=') + 1, it.indexOf("dp")).toInt())
                            it.endsWith("sf") -> Pair(NumericHelper.Rounding.SIGNIFICANT_FIGURES, it.substring(it.indexOf('=') + 1, it.indexOf("sf")).toInt())
                            else -> error("Specifies rounding does not make sense!")
                        }
                    }
                }
            }

            logger.infoFun(Core.getMethodName("[${args.joinToString(", ") { "\"$it\"" }}]")) { "Invoked with p=$prob, n=$attempts, k=$success, r=$round" }

            if (!prob.first) {
                error("I need the probability!")
            } else if (prob.second <= 0.0 || prob.second > 1.0) {
                error("Probability must be between 0.0 and 1.0!")
            } else if (attempts.second < 0 || success.second < 0) {
                error("You cannot try or succeed a negative amount of times!")
            } else if (attempts.second < success.second) {
                error("You can't succeed more times than you tried!")
            } else if (round.first == NumericHelper.Rounding.DECIMAL_PLACES && round.second < 0) {
                error("You cannot format a number to a negative number of decimal places!")
            } else if (round.first == NumericHelper.Rounding.SIGNIFICANT_FIGURES && round.second <= 0) {
                error("You cannot format a number to a negative number of significant figures!")
            }
        } catch (e: NumberFormatException) {
            return Result.Failure("One of the numbers is not formatted properly!")
        } catch (e: Exception) {
            return Result.Failure(e.message!!)
        }

        return Result.Success {
            val p = prob.second
            val n = attempts.second
            val k = success.second

            withDesc("With probability=$p, " +
                    "attempts=${if (attempts.first) n.toString() else "(not given)"}, " +
                    "successes=${if (success.first) k.toString() else "(not given)"}")

            appendField("Mean Attempts for First Success", formatReal(1 / p, round), true)
            appendField("Variance for First Success", formatReal((1 - p) / p.pow(2), round), true)

            appendField("Attempts for >50% Chance", "${minAttempts(0.5, p)}", true)
            appendField(">90% Chance", "${minAttempts(0.9, p)}", true)
            appendField(">99% Chance", "${minAttempts(0.99, p)}", true)

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
    }

    /**
     * Compute the minimum number of attempts in which an event with chance [p] will have at least [chance] of happening.
     */
    private fun minAttempts(chance: Double, p: Double): Int = ceil(log(1 - chance, 1 - p)).toInt()

    sealed class Result {
        class Failure(val message: String) : Result()
        class Success(val embeds: EmbedBuilder.() -> Unit) : Result()
    }
}