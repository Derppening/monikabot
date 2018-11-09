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

package com.derppening.monikabot.models.warframe

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.test.junit5.JUnit5Asserter.fail

class ManifestTest {
    private lateinit var jsonMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        jsonMapper = jacksonObjectMapper().apply {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }
    }

    @Test
    @DisplayName("Parse Export Manifest")
    fun testParseExportManifest() {
        val clazz = Manifest::class
        val companionClazz = clazz.companionObject
            ?: fail("Cannot find companion object")

        companionClazz.declaredMemberFunctions
            .find { it.name == "parseManifest" && it.valueParameters.isEmpty() }
            ?.apply {
                isAccessible = true
            }
            ?.call(clazz.companionObjectInstance)
            ?: fail("Cannot find field")
    }
}
