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

package com.derppening.monikabot.impl.experimental

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.util.FuzzyMatcher
import java.io.File
import java.nio.file.Paths

object EmoticonService : ILogger {
    fun findEmoticon(search: String): Result {
        return FuzzyMatcher(listOf(search), pairs.map { it.key }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().let { matches ->
            when (matches.size) {
                0 -> Result.Failure(Parser.HandleState.NOT_FOUND)
                1 -> Result.Success(pairs.entries.first { it.key == matches.first() }.value)
                else -> {
                    val matchList = pairs.filterValues {
                        matches.contains(it)
                    }.entries.joinToString("\n") {
                        "${it.key} - ${it.value}"
                    }
                    Result.Failure(Parser.HandleState.UNHANDLED, "Multiple Matches!\n\n$matchList")
                }
            }
        }
    }

    sealed class Result {
        class Success(val emote: String) : Result()
        class Failure(val state: Parser.HandleState, val message: String = "") : Result()
    }

    private fun readFromFile(): Map<String, String> =
            File(Paths.get("persistent/emoticons.txt").toUri())
                    .readLines()
                    .associateBy(
                            { it.takeWhile { it != '=' } },
                            { it.dropWhile { it != '=' }.drop(1) }
                    )

    private val pairs = readFromFile()
}