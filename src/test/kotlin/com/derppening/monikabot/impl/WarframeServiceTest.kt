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

import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.junit5.JUnit5Asserter.fail

class WarframeServiceTest {
    private lateinit var jsonMapper: ObjectMapper

    @BeforeEach
    fun prepareMapper() {
        jsonMapper = jacksonObjectMapper().apply {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }
    }

    @Test
    @DisplayName("Parse Drop Tables")
    fun testParseDropTables() {
        try {
            jsonMapper.apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }.readValue<WorldState>(URL("$DROP_TABLE_DATA_URL/all.json"))
        } catch (e: UnrecognizedPropertyException) {
            fail(e.message)
        }
    }

    @Disabled
    @Test
    @DisplayName("Parse Drop Tables (All)")
    fun testParseAllDropTables() {
        try {
            jsonMapper.readValue<WorldState>(URL("$DROP_TABLE_DATA_URL/all.json"))
        } catch (e: UnrecognizedPropertyException) {
            fail(e.message)
        }
    }

    @Test
    @DisplayName("Parse World State")
    fun testParseWorldState() {
        try {
            jsonMapper.apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }.readValue<WorldState>(URL(WORLDSTATE_URL))
        } catch (e: UnrecognizedPropertyException) {
            fail(e.message)
        }
    }

    @Disabled
    @Test
    @DisplayName("Parse World State (All)")
    fun testParseAllWorldState() {
        try {
            jsonMapper.readValue<WorldState>(URL(WORLDSTATE_URL))
        } catch (e: UnrecognizedPropertyException) {
            fail(e.message)
        }
    }

    companion object {
        private const val DROP_TABLE_DATA_URL = "https://raw.githubusercontent.com/WFCD/warframe-drop-data/gh-pages/data/"
        private const val WORLDSTATE_URL = "http://content.warframe.com/dynamic/worldState.php"
    }
}
