package cmds.warframe

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
            return jacksonObjectMapper().apply {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(URL("http://content.warframe.com/MobileExport/Manifest/ExportManifest.json"))
        }
    }
}