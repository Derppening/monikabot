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

package com.derppening.monikabot.impl.warframe

import com.derppening.monikabot.models.warframe.market.MarketManifest
import com.derppening.monikabot.models.warframe.market.MarketStats
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.Jsoup
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.junit5.JUnit5Asserter.fail

class MarketServiceTest {
    private lateinit var jsonMapper: ObjectMapper

    @BeforeEach
    fun prepareMapper() {
        jsonMapper = jacksonObjectMapper().apply {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }
    }

    @Test
    fun testParseMarketManifest() {
        val link = "https://warframe.market/"
        val jsonToParse = Jsoup.connect(link)
            .timeout(5000)
            .get()
            .select("#application-state")

        try {
            jsonMapper.readTree(jsonToParse.html())
                .get("items").get("en").let {
                    jsonMapper.readValue<List<MarketManifest>>(it.toString())
                }
        } catch (e: UnrecognizedPropertyException) {
            fail(e.message)
        }
    }

    @Test
    fun testParseItemStatistics() {
        val link = "https://warframe.market/items/ash_prime_blueprint/statistics"
        val jsonToParse = Jsoup.connect(link)
            .timeout(5000)
            .get()
            .select("#application-state")

        try {
            jsonMapper.readValue<MarketStats>(jsonToParse.html())
        } catch (e: UnrecognizedPropertyException) {
            fail(e.message)
        }
    }
}
