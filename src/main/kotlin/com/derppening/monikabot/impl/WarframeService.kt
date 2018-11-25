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
import com.derppening.monikabot.impl.warframe.DropService
import com.derppening.monikabot.models.warframe.droptable.DropTable
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.readText
import com.derppening.monikabot.util.helpers.setTimeout
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL
import kotlin.concurrent.timer
import kotlin.system.measureTimeMillis

object WarframeService : ILogger {
    private val jsonMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }

    private const val DROPTABLE_DATA_URL = "https://raw.githubusercontent.com/WFCD/warframe-drop-data/gh-pages/data/"
    private const val WORLDSTATE_URL = "http://content.warframe.com/dynamic/worldState.php"

    private var dropTableInfo = DropTable.Info()
    var dropTables = DropTable()
        private set
    var worldState = WorldState()
        private set

    val updateDropTablesTask = timer("Update Drop Table Timer", true, 0, 300000) { updateDropTables() }
    val updateWorldStateTask = timer("Update WorldState Timer", true, 0, 30000) { updateWorldState() }

    /**
     * Function to update drop tables.
     */
    private fun updateDropTables() {
        try {
            val infoJson = URL("$DROPTABLE_DATA_URL/info.json")
                .openConnection()
                .setTimeout(2000, 2000)
                .readText()
            val info = jsonMapper.readValue<DropTable.Info>(infoJson)

            if (info.hash == dropTableInfo.hash && info.timestamp == dropTableInfo.timestamp) {
                return
            }

            dropTableInfo = info

            val dropTableJson = URL("$DROPTABLE_DATA_URL/all.json")
                .openConnection()
                .setTimeout(2000, 2000)
                .readText()
            val timer = measureTimeMillis {
                dropTables = jsonMapper.readValue(dropTableJson)
            }

            if (timer >= 2000) {
                logger.infoFun(Core.getMethodName()) { "Parsing took $timer ms! Is server overloaded?" }
            }

            DropService.doCacheUpdate()
        } catch (tr: Throwable) {
            logger.warnFun(Core.getMethodName()) { "Unable to update! Will retry next cycle..." }
            tr.printStackTrace()
        }
    }

    /**
     * Function to update world state.
     */
    private fun updateWorldState() {
        try {
            val worldStateJson = URL(WORLDSTATE_URL)
                .openConnection()
                .setTimeout(2000, 2000)
                .readText()

            val timer = measureTimeMillis {
                worldState = jsonMapper.readValue(worldStateJson)
            }

            if (timer >= 2000) {
                logger.infoFun(Core.getMethodName()) { "Parsing took $timer ms! Is server overloaded?" }
            }
        } catch (tr: Throwable) {
            logger.warnFun(Core.getMethodName()) { "Unable to update! Will retry next cycle..." }
            tr.printStackTrace()
        }
    }
}