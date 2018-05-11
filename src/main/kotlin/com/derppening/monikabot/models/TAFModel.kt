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

package com.derppening.monikabot.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class TAFModel(
        val icao: String,
        val timestamp: Time,
        @JsonProperty("raw_text") val rawText: String,
        val forecast: List<Forecast>
) {
    data class Time(
            private val issued: String,
            private val bulletin: String,
            @JsonProperty("valid_from") private val validFrom: String,
            @JsonProperty("valid_to") private val validTo: String
    ) {
        val issuedTime: Instant by lazy {
            val format = DateTimeFormatter.ofPattern("dd'-'MM'-'uuuu '@' HH':'mmX")
            ZonedDateTime.parse(issued, format).toInstant()
        }

        val bulletinTime: Instant by lazy {
            val format = DateTimeFormatter.ofPattern("dd'-'MM'-'uuuu '@' HH':'mmX")
            ZonedDateTime.parse(issued, format).toInstant()
        }

        val validTime: Pair<Instant, Instant> by lazy {
            val format = DateTimeFormatter.ofPattern("dd'-'MM'-'uuuu '@' HH':'mmX")
            ZonedDateTime.parse(validFrom, format).toInstant() to ZonedDateTime.parse(validTo, format).toInstant()
        }
    }

    data class Forecast(
            val timestamp: Time,
            @JsonProperty("change_indicator") val changeIndicator: String?,
            val clouds: List<Cloud>?,
            @JsonProperty("time_becoming") private val timeBecoming: String?,
            val visibility: Visibility?,
            val wind: Wind?
    ) {
        val becomingTime: Instant? by lazy {
            val format = DateTimeFormatter.ofPattern("uuuu'-'MM'-'dd'T'HH':'mm':'ssX")
            ZonedDateTime.parse(timeBecoming, format).toInstant()
        }

        data class Time(
                @JsonProperty("forecast_from") private val forecastFrom: String,
                @JsonProperty("forecast_to") private val forecastTo: String
        ) {
            val forecastTime: Pair<Instant, Instant> by lazy {
                val format = DateTimeFormatter.ofPattern("dd'-'MM'-'uuuu '@' HH':'mmX")
                ZonedDateTime.parse(forecastFrom, format).toInstant() to ZonedDateTime.parse(forecastTo, format).toInstant()
            }
        }

        data class Cloud(
                val code: String?,
                val text: String?,
                @JsonProperty("base_feet_agl") val baseFeet: Double?,
                @JsonProperty("base_meters_agl") val baseMeters: Double?
        )

        data class Visibility(
                val miles: String,
                val meters: String
        )

        data class Wind(
                val degrees: Int,
                @JsonProperty("speed_kts") val speedKts: Int,
                @JsonProperty("speed_mph") val speedMPH: Int,
                @JsonProperty("speed_mps") val speedMPS: Int,
                @JsonProperty("gust_kts") val gustKts: Int?,
                @JsonProperty("gust_mph") val gustMPH: Int?,
                @JsonProperty("gust_mps") val gustMPS: Int?
        ) {
            fun format(): Wind {
                val gust = when {
                    gustKts != null -> gustKts
                    gustMPH != null -> (gustMPH * 0.868976).roundToInt()
                    gustMPS != null -> (gustMPS * 1.852).roundToInt()
                    else -> null
                }

                return Wind(degrees, speedKts, speedMPH, speedMPS, gust, null, null)
            }
        }
    }
}