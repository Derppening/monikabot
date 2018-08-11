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

import com.derppening.monikabot.core.ILogger

object RandomService : ILogger {
    fun generateRandom(args: List<String>): String {
        // TODO(Derppening): Use "--" instead of directly specifying
        return if (args.size == 1 && args[0].matches(Regex("dic?e"))) {
            rollDie()
        } else if (args.size == 1 && args[0].equals("coin", true)) {
            flipCoin()
        } else if (args[0].equals("list", true)) {
            randomList(args.drop(1))
        } else {
            generate(args)
        }
    }

    private fun rollDie(): String {
        return "You got a ${generateInt(1, 7)}!"
    }

    private fun flipCoin(): String {
        return "You got ${if (generateInt(0, 2) == 0) "tails" else "heads"}!"
    }

    private fun randomList(args: List<String>): String {
        return args.shuffled().firstOrNull()?.let { "You got $it!" }
                ?: "Give me items to randomize!"
    }

    private fun generate(p: List<String>): String {
        val args = p.toMutableList()

        val isReal = args.contains("real").also { args.removeIf { it.contains("real") } }
        args.remove("from")
        args.remove("to")

        if (args.size != 2) {
            return "Give me ${if (args.size > 2) "only " else ""}the minimum and maximum number!! >_>"
        }

        val min: Double
        val max: Double
        try {
            min = args[0].toDoubleOrNull() ?: error("Minimum number is not a number!")
            max = args[1].toDoubleOrNull() ?: error("Maximum number is not a number!")

            if (min >= max) error("Minimum number is bigger than the maximum!")
        } catch (e: Exception) {
            return "${e.message} >_>"
        }

        return if (isReal) {
            val n = generateReal(min, max)
            "You got $n!"
        } else {
            val n = generateInt(min.toInt(), (max + 1).toInt())
            "You got a $n!"
        }
    }

    private fun generateInt(min: Int, max: Int): Int = java.util.Random().nextInt(max - min) + min
    private fun generateReal(min: Double, max: Double): Double = (java.util.Random().nextDouble() * (max - min)) + min
}