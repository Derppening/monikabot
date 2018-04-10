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

package models.warframe

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL

class Manifest {
    val manifest = listOf<Asset>()

    class Asset {
        val uniqueName = ""
        val textureLocation = ""
        val fileTime = 0L
    }

    companion object {
        internal fun getImageLinkFromAssetLocation(uniqueName: String): String {
            val textureLocation = parseManifest().manifest.find { it.uniqueName == uniqueName }?.textureLocation ?: ""
            if (textureLocation.isBlank()) { return "" }
            return "http://content.warframe.com/MobileExport${textureLocation.replace("\\", "/")}"
        }

        internal fun findImageByRegex(nameRegex: Regex): String {
            val matches = parseManifest().manifest.filter { it.uniqueName.contains(nameRegex) }
            return if (matches.size != 1) {
                ""
            } else {
                getImageLinkFromAssetLocation(matches.first().uniqueName)
            }
        }

        private fun parseManifest(): Manifest {
            return jsonMapper.readValue(URL("http://content.warframe.com/MobileExport/Manifest/ExportManifest.json"))
        }

        private val jsonMapper = jacksonObjectMapper().apply {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }
    }
}