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

package com.derppening.monikabot.models.warframe.util

import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue

class MissionRewardDeserializer : JsonDeserializer<List<WorldState.MissionReward>>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): List<WorldState.MissionReward> {
        return if (parser.currentToken == JsonToken.START_OBJECT) {
            val mapper = ObjectMapper().apply {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue<WorldState.MissionReward>(parser.readValueAsTree<JsonNode>().toString())

            listOf(mapper)
        } else {
            ObjectMapper().apply {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(parser.readValueAsTree<JsonNode>().toString())
        }
    }
}