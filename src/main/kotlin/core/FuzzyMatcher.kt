/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package core

import core.BuilderHelper.buildMessage
import sx.blah.discord.handle.obj.IChannel

/**
 * Utility class for fuzzy matching strings.
 *
 * @param matchExpr List of "tags" for searching.
 * @param matchers List of strings for [matchExpr] to compare with.
 * @param action Additional options for fuzzy matching.
 */
class FuzzyMatcher(private val matchExpr: List<String>, private val matchers: List<String>, action: FuzzyMatcher.() -> Unit) : ILogger {
    private var emptyMatchHandler: () -> Unit = {}
    private var emptyMatchMessage: () -> Pair<String, IChannel?> = { "" to null }
    private var multipleMatchHandler: () -> Unit = {}
    private var multipleMatchMessage: () -> Pair<String, IChannel?> = { "" to null }
    private val regexOptions: MutableSet<RegexOption> = mutableSetOf()

    init {
        action(this)
    }

    /**
     * Action to execute when there is no match.
     */
    fun onEmptyMatch(action: () -> Unit) { emptyMatchHandler = action }

    /**
     * Message to display when there is no match.
     */
    fun emptyMatchMessage(action: () -> Pair<String, IChannel?>) { emptyMatchMessage = action }

    /**
     * Action to execute when there are multiple matches.
     */
    fun onMultipleMatch(action: () -> Unit) { multipleMatchHandler = action }

    /**
     * Message to display when there are multiple matches.
     */
    fun multipleMatchMessage(action: () -> Pair<String, IChannel?>) { multipleMatchMessage = action }

    /**
     * Sets additional regex options.
     */
    fun regex(vararg options: RegexOption) { regexOptions += options }

    /**
     * Attempts to match [matchExpr] with one of the entries in [matchers].
     *
     * @return The matchee if the match is unique; otherwise an empty string.
     */
    fun matchOne(): String {
        val matches = matches()

        return when (matches.size) {
            0 -> {
                val (messageRaw, channel) = emptyMatchMessage()

                val message = messageRaw.replace("{expr}", matchExpr.joinToString(" "))
                        .replace("{size}", matches.size.toString())
                        .replace(Regex("\\{(\\d+)(\\((.*)\\))?}", RegexOption.DOT_MATCHES_ALL)) {
                            matches.take(it.groupValues.getOrNull(1)?.toIntOrNull() ?: 5)
                                    .joinToString(it.groupValues.getOrNull(3) ?: " ")
                        }

                if (channel != null) {
                    buildMessage(channel) {
                        withContent(message)
                    }
                }

                emptyMatchHandler()
                ""
            }
            1 -> {
                matches.first()
            }
            else -> {
                val (messageRaw, channel) = multipleMatchMessage()

                val message = messageRaw.replace("{expr}", matchExpr.joinToString(" "))
                        .replace("{size}", matches.size.toString())
                        .replace(Regex("\\{(\\d+)(\\((.*)\\))?}", RegexOption.DOT_MATCHES_ALL)) {
                            matches.take(it.groupValues.getOrNull(1)?.toIntOrNull() ?: 5)
                                    .joinToString(it.groupValues.getOrNull(3) ?: " ")
                        }

                if (channel != null) {
                    buildMessage(channel) {
                        withContent(message)
                    }
                }

                multipleMatchHandler()
                ""
            }
        }
    }

    /**
     * Returns all matches.
     */
    fun matches(): List<String> {
        matchers.firstOrNull { it.equals(matchExpr.joinToString(" "), true) }?.let {
            return listOf(it)
        }

        val searchTags = matchExpr
        val searchResult = mutableMapOf<String, Int>()
        searchTags.forEach { tag ->
            val results = matchers.filter {
                val regex = tag.replace("*", ".+")
                if (tag.contains("*") && !tag.contains(" ")) {
                    it.split(" ").any {
                        it.matches(regex.toRegex(regexOptions))
                    }
                } else {
                    it.contains(regex.toRegex(regexOptions))
                }
            }
            results.forEach {
                searchResult[it] = searchResult[it]?.plus(1) ?: 1
            }
        }

        val sortedResults = searchResult.entries.sortedByDescending { it.value }
        return sortedResults.filter { it.value == sortedResults.first().value }.map { it.key }
    }
}
