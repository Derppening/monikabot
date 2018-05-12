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
import com.derppening.monikabot.util.FuzzyMatcher
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.openAndSetUserAgent
import com.derppening.monikabot.util.helpers.readText
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.net.URL

object DogService : ILogger {
    private val jsonMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }

    /**
     * Returns a formatted list of all breeds.
     *
     * @param breed If specified, return list of subbreeds instead.
     */
    fun list(breed: String = ""): ListResult {
        val list = getList()

        return if (breed.isNotBlank()) {
            val breeds = findBreedFuzzy(breed)
            when (breeds.size) {
                0 -> {
                    ListResult.Failure("I can't find a breed named \"$breed\"!")
                }
                1 -> {
                    val subbreedList = getBreedList(breeds.first())
                    buildEmbed {
                        withTitle("List of Subbreeds of ${breeds.first().capitalize()}")
                        withDesc(subbreedList.joinToString("\n") { it.capitalize() })

                        withFooterText("Total: ${subbreedList.size}")
                    }.build().let { ListResult.Success(it) }
                }
                else -> {
                    ("Multiple breeds match your given search! Including:\n\n" +
                            breeds.take(5).joinToString("\n") { "- ${it.capitalize()}" } +
                            if (breeds.size > 5) {
                                "\n\n...And ${breeds.size - 5} more results."
                            } else {
                                ""
                            }).let {
                        ListResult.Failure(it)
                    }
                }
            }
        } else {
            buildEmbed {
                withTitle("List of Breeds")
                withDesc(list.joinToString("\n") { it.capitalize() })

                withFooterText("Total: ${list.size}")
            }.build().let { ListResult.Success(it) }
        }
    }

    /**
     * Finds breeds using the given [keyword].
     *
     * @return [ShowResult.Success] if only one match is found; Otherwise [ShowResult.Failure].
     */
    fun getBreed(keyword: String): ShowResult {
        val breeds = findBreedFuzzy(keyword)

        return when (breeds.size) {
            0 -> ShowResult.Failure("I can't find a breed named \"$keyword\"!")
            1 -> ShowResult.Success(getBreedPic(breeds.first()))
            else -> {
                ("Multiple breeds match your given search! Including:\n\n" +
                        breeds.take(5).joinToString("\n") { "- ${it.capitalize()}" } +
                        if (breeds.size > 5) {
                            "\n\n...And ${breeds.size - 5} more results."
                        } else {
                            ""
                        }).let {
                    ShowResult.Failure(it)
                }
            }
        }
    }

    /**
     * @return List of all breeds supported by the API.
     */
    private fun getList(): List<String> {
        val page = "https://dog.ceo/api/breeds/list"
        val json = URL(page).openAndSetUserAgent().readText()

        return jsonMapper.readTree(json)
                .get("message")
                .let {
                    jsonMapper.readValue(it.toString())
                }
    }

    /**
     * Fuzzily matches all breeds using the given [keyword].
     *
     * @return A list of all matching breeds.
     */
    private fun findBreedFuzzy(keyword: String): List<String> {
        val list = getList()

        return FuzzyMatcher(keyword.split(' '), list) {
            regex(RegexOption.IGNORE_CASE)
        }.matches()
    }

    /**
     * Finds subbreeds of [breed] using the given [keyword].
     *
     * @return [ShowResult.Success] if only one match is found; Otherwise [ShowResult.Failure].
     */
    fun getSubbreed(breed: String, keyword: String): ShowResult {
        val breedActual = findBreedFuzzy(breed).also {
            if (it.size != 1) {
                return getBreed(breed)
            }
        }.first()
        val subbreedList = findSubbreedFuzzy(breedActual, keyword)

        return when (subbreedList.size) {
            0 -> ShowResult.Failure("I can't find a subbreed named \"$keyword\"!")
            1 -> ShowResult.Success(getBreedPic(breedActual, subbreedList.first()))
            else -> {
                ("Multiple breeds match your given search! Including:\n\n" +
                        subbreedList.take(5).joinToString("\n") { "- ${it.capitalize()}" } +
                        if (subbreedList.size > 5) {
                            "\n\n...And ${subbreedList.size - 5} more results."
                        } else {
                            ""
                        }).let {
                    ShowResult.Failure(it)
                }
            }
        }
    }

    /**
     * Fuzzily matches all subbreeds of [breed] using the given [keyword].
     *
     * @return A list of all matching breeds.
     */
    private fun findSubbreedFuzzy(breed: String, keyword: String): List<String> {
        val list = getBreedList(breed)

        return FuzzyMatcher(keyword.split(' '), list) {
            regex(RegexOption.IGNORE_CASE)
        }.matches()
    }

    /**
     * @return List of all subbreeds of [subbreed] supported by the API.
     */
    private fun getBreedList(subbreed: String): List<String> {
        val page = "https://dog.ceo/api/breed/$subbreed/list"
        val json = URL(page).openAndSetUserAgent().readText()

        return jsonMapper.readTree(json)
                .get("message")
                .let {
                    jsonMapper.readValue(it.toString())
                }
    }

    /**
     * @return URL to a random photo of a dog.
     */
    fun getRandomPic(): String {
        val page = "https://dog.ceo/api/breeds/image/random"
        val json = URL(page).openAndSetUserAgent().readText()

        return jsonMapper.readTree(json).get("message").asText()
    }

    /**
     * @return URL to a dog of [breed] (and [subbreed] if specified).
     */
    private fun getBreedPic(breed: String, subbreed: String = ""): String {
        val page = if (subbreed.isBlank()) {
            "https://dog.ceo/api/breed/$breed/images/random"
        } else {
            "https://dog.ceo/api/breed/$breed/$subbreed/images/random"
        }
        val json = URL(page).openAndSetUserAgent().readText()

        return jsonMapper.readTree(json).get("message").asText()
    }

    sealed class ListResult {
        class Success(val embed: EmbedObject) : ListResult()
        class Failure(val message: String) : ListResult()
    }

    sealed class ShowResult {
        class Success(val link: String) : ShowResult()
        class Failure(val message: String) : ShowResult()
    }
}