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

data class METARModel(
        val icao: String,
        val name: String,
        private val observed: String,
        @JsonProperty("raw_text") val rawText: String,
        val barometer: Pressure?,
        val ceiling: CloudCeiling?,
        val clouds: List<Cloud>?,
        val conditions: List<Condition>?,
        val dewpoint: Temperature?,
        val elevation: Elevation?,
        @JsonProperty("flight_category") val flightCategory: String?,
        @JsonProperty("humidity_percent") val humidityPercent: Int?,
        @JsonProperty("rain_in") val rainIn: Int?,
        @JsonProperty("snow_in") val snowIn: Int?,
        val temperature: Temperature?,
        val visibility: Visibility?,
        val wind: Wind?
) {
    val date: Instant by lazy {
        val format = DateTimeFormatter.ofPattern("dd'-'MM'-'uuuu '@' HH':'mmX")
        ZonedDateTime.parse(observed, format).toInstant()
    }

    data class Pressure(
            val hg: Double?,
            val kpa: Double?,
            val mb: Double?
    ) {
        fun format(): Pressure {
            val mb = when {
                mb != null -> mb
                kpa != null -> kpa * 10
                hg != null -> hg * 1.3332239
                else -> throw IllegalStateException("Pressure contains no non-null values")
            }

            return Pressure(null, null, mb)
        }
    }

    data class CloudCeiling(
            val code: String?,
            val text: String?,
            @JsonProperty("feet_agl") val feetAGL: Double?,
            @JsonProperty("meters_agl") val metersAGL: Double?
    ) {
        fun format(): CloudCeiling {
            val feet = when {
                feetAGL != null -> feetAGL
                metersAGL != null -> metersAGL / 0.3048
                else -> throw IllegalStateException("CloudCeiling contains no non-null values")
            }

            return CloudCeiling(code, text, feet, null)
        }
    }

    data class Cloud(
            val code: String?,
            val text: String?,
            @JsonProperty("base_feet_agl") val baseFeet: Double?,
            @JsonProperty("base_meters_agl") val baseMeters: Double?
    ) {
        fun format(): Cloud {
            val feet = when {
                baseFeet != null -> baseFeet
                baseMeters != null -> baseMeters / 0.3048
                else -> throw IllegalStateException("Cloud contains no non-null values")
            }

            return Cloud(code, text, feet, null)
        }
    }

    data class Condition(
            val code: String?,
            val text: String?
    )

    data class Elevation(
            val feet: Int?,
            val meters: Int?
    ) {
        fun format(): Elevation {
            val feet = when {
                feet != null -> feet
                meters != null -> (meters / 0.3048).roundToInt()
                else -> throw IllegalStateException("Elevation contains no non-null values")
            }

            return Elevation(feet, null)
        }
    }

    data class Visibility(
            val feet: String?,
            val meters: String?
    )

    data class Temperature(
            val celsius: Int?,
            val fahrenheit: Int?
    ) {
        fun format(): Temperature {
            val cel = when {
                celsius != null -> celsius
                fahrenheit != null -> (fahrenheit - 32) * 5 / 9
                else -> throw IllegalStateException("Temperature contains no non-null values")
            }

            return Temperature(cel, null)
        }
    }

    data class Wind(
            val degrees: Int?,
            @JsonProperty("speed_kts") val speedKts: Int?,
            @JsonProperty("speed_mph") val speedMPH: Int?,
            @JsonProperty("speed_mps") val speedMPS: Int?,
            @JsonProperty("gust_kts") val gustKts: Int?,
            @JsonProperty("gust_mph") val gustMPH: Int?,
            @JsonProperty("gust_mps") val gustMPS: Int?
    ) {
        fun format(): Wind {
            val speed = when {
                speedKts != null -> speedKts
                speedMPH != null -> (speedMPH * 0.868976).roundToInt()
                speedMPS != null -> (speedMPS * 1.852).roundToInt()
                else -> null
            }

            val gust = when {
                gustKts != null -> gustKts
                gustMPH != null -> (gustMPH * 0.868976).roundToInt()
                gustMPS != null -> (gustMPS * 1.852).roundToInt()
                else -> null
            }

            return Wind(degrees, speed, null, null, gust, null, null)
        }
    }
}
