package cmds.warframe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL
import java.time.Instant
import java.util.*

class WorldState {
    val worldSeed = ""
    val version = 0
    val mobileVersion = ""
    val buildLabel = ""
    @JsonDeserialize(using = TimeSecondsDeserializer::class)
    val time = Instant.EPOCH
    @JsonDeserialize(using = TimeSecondsDeserializer::class)
    val date = Instant.EPOCH
    val events = listOf<Event>()
    val goals = listOf<Any>()
    val alerts = listOf<Alert>()
    val sorties = listOf<Sorties>()
    val syndicateMissions = listOf<SyndicateMission>()
    val activeMissions = listOf<ActiveMission>()
    val globalUpgrades = listOf<Any>()
    val flashSales = listOf<FlashSale>()
    val invasions = listOf<Invasion>()
    val hubEvents = listOf<Any>()
    val badlandNodes = listOf<BadlandNode>()
    val nodeOverrides = listOf<NodeOverride>()
    val voidTraders = listOf<VoidTrader>()
    val primeAccessAvailability = PrimeVaultAvailability()
    val primeVaultAvailabilities = listOf<PrimeVaultAvailability>()
    val dailyDeals = listOf<DailyDeal>()
    val libraryInfo = LibraryInfo()
    val pvpChallengeInstances = listOf<PVPChallengeInstance>()
    val persistentEnemies = listOf<Any>()
    val pvpAlternativeModes = listOf<Any>()
    val pvpActiveTournaments = listOf<Any>()
    val projectPct = listOf<Double>()
    val constructionProjects = listOf<Any>()
    val twitchPromos = listOf<Any>()

    class Event {
        @JsonProperty("_id")
        val id = ID()
        val messages = listOf<Message>()
        val prop = ""
        val date = Date()
        val eventStartDate = Date()
        val eventEndDate = Date()
        val imageUrl = ""
        val priority = false
        val mobileOnly = false

        class Message {
            val languageCode = Locale.ROOT
            val message = ""
        }
    }

    class Alert {
        @JsonProperty("_id")
        val id = ID()
        val activation = Date()
        val expiry = Date()
        val missionInfo = MissionInfo()

        class MissionInfo {
            val missionType = ""
            val faction = ""
            val location = ""
            val levelOverride = ""
            val enemySpec = ""
            val extraEnemySpec = ""
            val vipAgent = ""
            val customAdvancedSpawners = listOf<String>()
            val minEnemyLevel = 0
            val maxEnemyLevel = 0
            val difficulty = 0.0
            val seed = 0
            val maxWaveNum = 0
            val archwingRequired = false
            @JsonProperty("isSharkwingMission")
            val isSharkwingMission = false
            val missionReward = MissionReward()
        }
    }

    class Sorties {
        @JsonProperty("_id")
        val id = ID()
        val activation = Date()
        val expiry = Date()
        val boss = ""
        val reward = ""
        val extraDrops = listOf<Any>()
        val seed = 0
        val variants = listOf<Variant>()
        val twitter = false

        class Variant {
            val missionType = ""
            val modifierType = ""
            val node = ""
            val tileset = ""
        }
    }

    class SyndicateMission {
        @JsonProperty("_id")
        val id = ID()
        val activation = Date()
        val expiry = Date()
        val tag = ""
        val seed = 0
        val nodes = listOf<String>()
        val jobs = listOf<Job>()

        class Job {
            val jobType = ""
            val rewards = ""
            val masteryReq = 0
            val minEnemyLevel = 0
            val maxEnemyLevel = 0
            val xpAmounts = listOf<Int>()
        }
    }

    class ActiveMission {
        @JsonProperty("_id")
        val id = ID()
        val region = 0
        val seed = 0
        val activation = Date()
        val expiry = Date()
        val node = ""
        val missionType = ""
        val modifier = ""
    }

    class FlashSale {
        val typeName = ""
        val startDate = Date()
        val endDate = Date()
        val featured = false
        val popular = false
        val showInMarket = false
        val bannerIndex = 0
        val discount = 0
        val regularOverride = 0
        val premiumOverride = 0
        val bogoBuy = 0
        val bogoGet = 0
    }

    class Invasion {
        @JsonProperty("_id")
        val id = ID()
        val faction = ""
        val node = ""
        val count = 0
        val goal = 0
        val locTag = ""
        val completed = false
        val attackerReward: JsonNode? = null
        val attackerMissionInfo = MissionInfo()
        val defenderReward = MissionReward()
        val defenderMissionInfo = MissionInfo()
        val activation = Date()

        class MissionInfo {
            val seed = 0
            val faction = ""
            val missionReward = listOf<Any>()
        }
    }

    class NodeOverride {
        @JsonProperty("_id")
        val id = ID()
        val node = ""
        val hide = false
    }

    class BadlandNode {
        @JsonProperty("_id")
        val id = ID()
        val defenderInfo = DefenderInfo()
        val node = ""

        class DefenderInfo {
            @JsonProperty("IsAlliance")
            val isAlliance = false
            val id = ID()
            val name = ""
            val motd = ""
            val deployerName = ""
            val deployerClan = ""
        }
    }

    class VoidTrader {
        @JsonProperty("_id")
        val id = ID()
        val activation = Date()
        val expiry = Date()
        val character = ""
        val node = ""
    }

    class DailyDeal {
        val storeItem = ""
        val activation = Date()
        val expiry = Date()
        val discount = 0
        val originalPrice = 0
        val salePrice = 0
        val amountTotal = 0
        val amountSold = 0
    }

    class LibraryInfo {
        val lastCompletedTargetType = ""
    }

    class PVPChallengeInstance {
        @JsonProperty("_id")
        val id = ID()
        val challengeTypeRefID = ""
        val startDate = Date()
        val endDate = Date()
        val params = listOf<Param>()
        @JsonProperty("isGenerated")
        val isGenerated = false
        @JsonProperty("PVPMode")
        val PVPMode = ""
        val subChallenges = listOf<ID>()
        val category = ""

        class Param {
            val n = ""
            val p = 0
            val v = 0
        }
    }

    class ID {
        @JsonProperty("\$oid")
        val oid: String = ""
    }

    class Date {
        @JsonProperty("\$date")
        val date = DateInner()

        class DateInner {
            @JsonProperty("\$numberLong")
            @JsonDeserialize(using = TimeMillisDeserializer::class)
            val numberLong = Instant.EPOCH
        }
    }

    class MissionReward {
        val credits = 0
        val items = listOf<String>()
        val countedItems = listOf<CountedItems>()

        class CountedItems {
            val itemType = ""
            val itemCount = 0
        }
    }

    class PrimeVaultAvailability {
        val state = ""
    }

    class TimeSecondsDeserializer : JsonDeserializer<Instant>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext?): Instant {
            return Instant.ofEpochSecond(parser.valueAsLong)
        }
    }

    class TimeMillisDeserializer : JsonDeserializer<Instant>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext?): Instant {
            return Instant.ofEpochMilli(parser.valueAsLong)
        }
    }

    companion object {
        internal fun getArcaneInfo(arcane: String): Arcane {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/arcanes.json"))
                        .find { arcane.matches(it.get("regex").asText().toRegex()) }
                        ?.let {
                            ObjectMapper().apply {
                                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                            }.readValue<Arcane>(it.toString())
                        } ?: Arcane()
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
                            ObjectMapper().apply {
                                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                            }.readValue(it.toString())
                        }
            } catch (e: Exception) {
                SolNode()
            }
        }

        internal fun getSortieModifier(modifier: String): SortieModifier {
            return try {
                val tree = jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/sortieData.json"))

                SortieModifier(tree.get("modifierTypes").get(modifier).asText(), tree.get("modifierDescriptions").get(modifier).asText())
            } catch (e: Exception) {
                SortieModifier("", "")
            }
        }

        internal fun getSortieBoss(boss: String): SortieBoss {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/sortieData.json"))
                        .get("bosses")
                        .get(boss)
                        .let {
                            ObjectMapper().apply {
                                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                            }.readValue(it.toString())
                        }
            } catch (e: Exception) {
                SortieBoss()
            }
        }

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

        internal fun getWarframeInfo(warframe: String): Warframe {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/warframes.json"))
                        .find { warframe.matches(it.get("regex").asText().toRegex()) }
                        ?.let {
                            ObjectMapper().apply {
                                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                            }.readValue<Warframe>(it.toString())
                        } ?: Warframe()
            } catch (e: Exception) {
                Warframe()
            }
        }

        internal fun getWeaponInfo(weapon: String): Weapon {
            return try {
                jacksonObjectMapper().apply {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                }.readTree(URL("$worldStateDataUrl/weapons.json"))
                        .find { weapon.matches(it.get("regex").asText().toRegex()) }
                        ?.let {
                            ObjectMapper().apply {
                                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                            }.readValue<Weapon>(it.toString())
                        } ?: Weapon()
            } catch (e: Exception) {
                Weapon()
            }
        }

        class Arcane {
            val regex = Regex("")
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

        class SortieBoss {
            val name = ""
            val faction = ""
        }

        data class SortieModifier(val type: String, val description: String)

        class Warframe {
            val regex = Regex("")
            val name = ""
            val url = ""
            val mr = 0
            @JsonDeserialize(using = RankedDeserializer::class)
            val health = Pair(0.0, 0.0)
            @JsonDeserialize(using = RankedDeserializer::class)
            val shield = Pair(0.0, 0.0)
            @JsonDeserialize(using = RankedDeserializer::class)
            val armor = Pair(0.0, 0.0)
            @JsonDeserialize(using = RankedDeserializer::class)
            val power = Pair(0.0, 0.0)
            val speed = 0.0
            val conclave = 0
            val polarities = listOf<String>()
            val aura = ""
            val description = ""
            val info = ""
            val thumbnail = ""
            val location = ""
            val color = 0
            @JsonProperty("prime_url")
            val primeURL = ""
            @JsonProperty("prime_mr")
            val primeMR = 0
            @JsonProperty("prime_health")
            @JsonDeserialize(using = RankedDeserializer::class)
            val primeHealth = Pair(0.0, 0.0)
            @JsonProperty("prime_shield")
            @JsonDeserialize(using = RankedDeserializer::class)
            val primeShield = Pair(0.0, 0.0)
            @JsonProperty("prime_armor")
            @JsonDeserialize(using = RankedDeserializer::class)
            val primeArmor = Pair(0.0, 0.0)
            @JsonProperty("prime_speed")
            val primeSpeed = 0.0
            @JsonProperty("prime_power")
            @JsonDeserialize(using = RankedDeserializer::class)
            val primePower = Pair(0.0, 0.0)
            @JsonProperty("prime_conclave")
            val primeConclave = 0
            @JsonProperty("prime_polarities")
            val primePolarities = listOf<String>()
            @JsonProperty("prime_aura")
            val primeAura = ""

            class RankedDeserializer : JsonDeserializer<Pair<Double, Double>>() {
                override fun deserialize(parser: JsonParser, context: DeserializationContext?): Pair<Double, Double> {
                    return if (parser.valueAsString.any { it == '/' }) {
                        val pairStr = parser.valueAsString.split('/').map { it.filter { it.isDigit() || it == '.' } }
                        Pair(pairStr[0].toDouble(), pairStr[1].toDoubleOrNull()
                                ?: pairStr[0].toDouble())
                    } else {
                        Pair(0.0, 0.0)
                    }
                }
            }
        }

        class Weapon {
            val regex = Regex("")
            val name = ""
            val url = ""
            val mr = 0
            val type = ""
            val subtype = ""
            val noise = ""
            @JsonProperty("riven_disposition")
            val rivenDisposition = 0
            @JsonProperty("crit_chance")
            val critChance = 0.0
            @JsonProperty("crit_mult")
            val critMult = 0.0
            @JsonProperty("status_chance")
            val statusChance = 0.0
            val polarities = listOf<String>()
            val thumbnail = ""
            val speed = 0.0
            val ammo = 0
            val accuracy = ""
            val magazine = 0
            val reload = 0
            val projectile = ""
            val rate = 0.0
            @JsonDeserialize(using = DoubleStringPairDeserializer::class)
            val damage = Pair(0.0, "")
            val impact = 0.0
            val slash = 0.0
            val puncture = 0.0
            val trigger = ""
            @JsonDeserialize(using = DoubleStringPairDeserializer::class)
            val flight = Pair(0.0, "")
            @JsonDeserialize(using = DoubleStringPairDeserializer::class)
            val slide = Pair(0.0, "")
            @JsonDeserialize(using = DoubleStringPairDeserializer::class)
            val jump = Pair(0.0, "")
            @JsonDeserialize(using = DoubleStringPairDeserializer::class)
            val wall = Pair(0.0, "")
            val channeling = 0.0
            val stancePolarity = ""

            class DoubleStringPairDeserializer : JsonDeserializer<Pair<Double, String>>() {
                override fun deserialize(parser: JsonParser, context: DeserializationContext?): Pair<Double, String> {
                    return if (parser.valueAsString.any { !it.isDigit() || it != '.' }) {
                        val numeric = parser.valueAsString.filter { it.isDigit() || it == '.' }
                        val string = parser.valueAsString.filterNot { it.isDigit() || it == '.' }.trim()
                        Pair(numeric.toDoubleOrNull() ?: 0.0, string)
                    } else {
                        Pair(parser.valueAsDouble, "")
                    }
                }
            }
        }

        private const val worldStateDataUrl = "https://raw.githubusercontent.com/WFCD/warframe-worldstate-data/master/data"
    }
}