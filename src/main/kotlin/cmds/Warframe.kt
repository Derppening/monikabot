package cmds

import cmds.warframe.Market
import cmds.warframe.News
import cmds.warframe.WorldState
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.Client
import core.IChannelLogger
import core.IConsoleLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.net.URL
import kotlin.concurrent.timer
import kotlin.system.measureTimeMillis

object Warframe : IBase, IChannelLogger, IConsoleLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, false)
            return Parser.HandleState.HANDLED
        }

        return when (args[0]) {
            "news" -> News.handler(event)
            "market" -> Market.handler(event)
            else -> {
                help(event, false)
                Parser.HandleState.HANDLED
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `warframe`")
                withDesc("Wrapper for Warframe-related commands.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```warframe [subcommand] [args]```", false)
                appendField("Subcommand: `news`", "Displays the latest Warframe news, same as the news segment in the orbiter.", false)
                appendField("Subcommand: `market`", "Displays market information about an item.", false)
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
        }
    }

    object Unobfuscate {
        internal fun getArcaneInfo(regex: String): Arcane {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readValue<List<Arcane>>(URL("$worldStateDataUrl/arcanes.json"))
                        .find { it.regex == regex || it.name == regex } ?: Arcane()
            } catch (e: Exception) {
                Arcane()
            }
        }

        internal fun getFactionString(faction: String): String {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/factionsData.json"))
                        .get(faction)
                        .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        internal fun getFissureModifier(tier: String): String {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/fissureModifiers.json"))
                        .get(tier)
                        .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        internal fun getLanguageFromAsset(encoded: String): String {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/languages.json"))
                        .get(encoded)
                        .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        internal fun getMissionType(missionType: String): String {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/missionTypes.json"))
                        .get(missionType)
                        .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        internal fun getSolNode(solNode: String): SolNode {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/solNodes.json"))
                        .get(solNode)
                        .let {
                            ObjectMapper().readValue(it.toString())
                        }
            } catch (e: Exception) {
                SolNode()
            }
        }

        // TODO(Derppening): getSortie...

        internal fun getSyndicateName(syndicate: String): String {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/syndicatesData.json"))
                        .get(syndicate)
                        .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        internal fun getUpgradeType(upgrade: String): String {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/upgradeTypes.json"))
                        .get(upgrade)
                        .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        // TODO(Derppening): getWarframe... and getWeapon...
    }

    private fun updateWorldState() {
        if (!Client.isReady) {
            return
        }

        val timer = measureTimeMillis {
            worldState = jacksonObjectMapper().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            }.readValue(URL(worldStateUrl))
        }

        logger.debug("updateWorldState(): Parse WorldState took ${timer}ms")
    }
    class Arcane {
        val regex = ""
        val name = ""
        val effect = ""
        val rarity = ""
        val location = ""
        val thumbnail = ""
        val info = ""
    }

    class SolNode {
        val value = ""
        val enemy = ""
        val type = ""
    }

    val updateWorldStateTask = timer("Update WorldState Timer", true, 0, 30000) { updateWorldState() }

    private const val worldStateDataUrl = "https://raw.githubusercontent.com/WFCD/warframe-worldstate-data/master/data"
    private const val worldStateUrl = "http://content.warframe.com/dynamic/worldState.php"

    var worldState = WorldState()
        private set
}