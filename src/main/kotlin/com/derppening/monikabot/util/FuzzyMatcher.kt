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

package com.derppening.monikabot.util

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import org.apache.commons.text.similarity.LevenshteinDistance
import sx.blah.discord.handle.obj.IChannel

/**
 * Utility class for matching strings using Levenshtein Distance.
 *
 * @param str String to match.
 * @param matchers List of strings for [str] to compare with.
 * @param action Additional options for fuzzy matching.
 */
class FuzzyMatcher(private val str: String, private val matchers: List<String>, threshold: Int? = null, action: FuzzyMatcher.() -> Unit = {}) : ILogger {
    private var emptyMatchHandler: (String) -> Unit = {}
    private var emptyMatchMessage: (String) -> Pair<String, IChannel?> = { "" to null }
    private var multipleMatchHandler: (String, List<String>) -> Unit = { _, _ -> }
    private var multipleMatchMessage: (String, List<String>) -> Pair<String, IChannel?> = { _, _ -> "" to null }
    private val measurer = LevenshteinDistance(threshold)

    init {
        action(this)
    }

    /**
     * Action to execute when there is no match.
     *
     * p1 will be the list of all matches.
     */
    fun onEmptyMatch(action: (String) -> Unit) {
        emptyMatchHandler = action
    }

    /**
     * Message to display when there is no match.
     *
     * p1 will be the list of all matches.
     */
    fun emptyMatchMessage(action: (String) -> Pair<String, IChannel?>) {
        emptyMatchMessage = action
    }

    /**
     * Action to execute when there are multiple matches.
     *
     * p1 will be the expression, whereas p2 will be the list of all matches.
     */
    fun onMultipleMatch(action: (String, List<String>) -> Unit) {
        multipleMatchHandler = action
    }

    /**
     * Message to display when there are multiple matches.
     *
     * p1 will be the expression, whereas p2 will be the list of all matches.
     */
    fun multipleMatchMessage(action: (String, List<String>) -> Pair<String, IChannel?>) {
        multipleMatchMessage = action
    }

    /**
     * Attempts to match [matchExpr] with one of the entries in [matchers].
     *
     * @return The matchee if the match is unique; otherwise an empty string.
     */
    fun matchOne(): String {
        val matches = matches()

        return when (matches.size) {
            0 -> {
                val (message, channel) = emptyMatchMessage(str)

                if (channel != null) {
                    buildMessage(channel) {
                        content {
                            withContent(message)
                        }
                    }
                }

                emptyMatchHandler(str)
                ""
            }
            1 -> {
                matches.first()
            }
            else -> {
                val (message, channel) = multipleMatchMessage(str, matches)

                if (channel != null) {
                    buildMessage(channel) {
                        content {
                            withContent(message)
                        }
                    }
                }

                multipleMatchHandler(str, matches)
                ""
            }
        }
    }

    /**
     * Returns all closest matches.
     */
    fun matches(): List<String> {
        val searchResult = matchers.associate {
            it to measurer.apply(it, str)
        }

        val sortedResults = searchResult.entries.filterNot { it.value == -1 }.sortedBy { it.value }
        return sortedResults.filter { it.value == sortedResults.first().value }.map { it.key }
    }
}