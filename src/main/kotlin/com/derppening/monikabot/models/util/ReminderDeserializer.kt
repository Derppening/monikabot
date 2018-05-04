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

package com.derppening.monikabot.models.util

import com.derppening.monikabot.core.Core.removeQuotes
import com.derppening.monikabot.impl.ReminderService
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant

class ReminderDeserializer : JsonDeserializer<ReminderService.Timer>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): ReminderService.Timer {
        val mapper = ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.readValue<JsonNode>(parser?.readValueAsTree<JsonNode>().toString())
        return ReminderService.Timer(mapper["timerName\$monikabot_main"].toString().removeQuotes(),
                Instant.ofEpochSecond(mapper["expiryDateTime\$monikabot_main"]["epochSecond"].longValue()),
                mapper["userID\$monikabot_main"].longValue())
    }
}