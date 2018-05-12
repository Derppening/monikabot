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
import com.derppening.monikabot.models.TAFModel
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.net.HttpURLConnection
import java.net.URL

object TAFService : ILogger {
    private val apiKey = Core.checkwxKey ?: run {
        logger.warn("Cannot load CheckWX API key! METAR services will not be available")
        ""
    }

    private fun getForICAO(icao: String): TAFModel {
        check(apiKey.isNotEmpty()) { "No CheckWX Key - METAR functionality is disabled" }

        val url = URL("https://api.checkwx.com/taf/$icao/decoded")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 5000
        connection.setRequestProperty("X-API-Key", apiKey)

        val jsonResult = connection.inputStream.bufferedReader().readText()

        check(connection.responseCode == 200) { "Server did not respond with OK - ${connection.responseMessage}" }

        return jsonResult.let {
            val mapper = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }
            mapper.readTree(it).also {
                val results = it.get("results").asInt()
                check(results == 1) { "More than one aerodome found." }
                check(!it.get("data").toString().contains("$icao Invalid Station ICAO", true)) { "No aerodomes with ICAO $icao found." }
            }.let {
                mapper.readValue<List<TAFModel>>(it.get("data").toString())
            }.first()
        }
    }

    fun toEmbed(icao: String): List<EmbedObject> {
        val taf = try {
            check(icao.length == 4) { "ICAO should consist of 4 characters." }
            getForICAO(icao)
        } catch (e: Exception) {
            e.printStackTrace()
            val embed = buildEmbed {
                withTitle("TAF for ${icao.toUpperCase()}")
                withDesc(e.message)
            }.build()
            return listOf(embed)
        }

        return taf.forecast.map {
            buildEmbed {
                withTitle("TAF for ${icao.toUpperCase()}")
                withDesc("Forecast Period: ${it.timestamp.forecastTime.first} to ${it.timestamp.forecastTime.second}")

                it.wind?.format()?.also {
                    it.degrees.also { appendField("Wind Direction", "$it°", true) }
                    it.speedKts.also { appendField("Wind Spped", "$it kts", true) }
                    it.gustKts?.also { appendField("Gusts Speed", "$it kts", true) }
                }

                it.visibility?.also {
                    it.meters.also { appendField("Visibility", "$it meters", false) }
                }

                it.clouds?.also {
                    val str = it.joinToString("\n") {
                        if (it.code != "SKC") {
                            "${it.text} @ ${it.baseFeet} ft"
                        } else {
                            it.text ?: "???"
                        }
                    }
                    appendField("Cloud Information", str, false)
                }

                withFooterText("Bulletin Time")
                withTimestamp(taf.timestamp.bulletinTime)
            }.build()
        }
    }
}