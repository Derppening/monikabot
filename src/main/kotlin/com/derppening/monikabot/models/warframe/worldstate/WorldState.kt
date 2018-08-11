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

package com.derppening.monikabot.models.warframe.worldstate

import com.derppening.monikabot.models.warframe.util.DoubleStringPairDeserializer
import com.derppening.monikabot.models.warframe.util.MissionRewardDeserializer
import com.derppening.monikabot.models.warframe.util.RankedDeserializer
import com.derppening.monikabot.models.warframe.util.TimeSecondsDeserializer
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
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
    val goals = listOf<Goal>()
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
    val persistentEnemies = listOf<PersistentEnemies>()
    val pvpAlternativeModes = listOf<Any>()
    val pvpActiveTournaments = listOf<Any>()
    val projectPct = listOf<Double>()
    val constructionProjects = listOf<Any>()
    val twitchPromos = listOf<TwitchPromo>()

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
        val eventLiveURL = ""

        class Message {
            val languageCode = Locale.ROOT
            val message = ""
        }
    }

    class Goal {
        @JsonProperty("_id")
        val id = ID()
        val fomorian = false
        val activation = Date()
        val expiry = Date()
        val healthPct = 0.0
        val victimNode = ""
        val regionIDx = 0
        val count = 0
        val goal = 1
        val success = 0
        val personal = false
        val best = false
        val scoreVar = ""
        val scoreMaxTag = ""
        val clampNodeScores = false
        val node = ""
        val missionKeyName = ""
        val faction = ""
        val desc = ""
        val tooltip = ""
        val icon = ""
        val regionDrops = listOf<Any>()
        val archwingDrops = listOf<String>()
        val scoreLocTag = ""
        val tag = ""
        val missionInfo = Alert.MissionInfo()
        val continuousHubEvent = ContinuousHubEvent()
        val jobAffiliationTag = ""
        val reward = Reward()
        val jobs = listOf<Job>()
        val transmission = ""
        val instructionalItem = ""

        class Reward {
            val credits = 0
            val items = listOf<String>()
        }

        class ContinuousHubEvent {
            val transmission = ""
            val activation = Date()
            val expiry = Date()
            val repeatInterval = 0
        }
    }

    class Alert {
        @JsonProperty("_id")
        val id = ID()
        val activation = Date()
        val expiry = Date()
        val missionInfo = MissionInfo()
        val forceUnlock = false
        val tag = ""

        class MissionInfo {
            val descText = ""
            val missionType = ""
            val faction = ""
            val location = ""
            val levelOverride = ""
            val enemySpec = ""
            val extraEnemySpec = ""
            val vipAgent = ""
            val leadersAlwaysAllowed = false
            val customAdvancedSpawners = listOf<String>()
            val minEnemyLevel = 0
            val maxEnemyLevel = 0
            val difficulty = 0.0
            val seed = 0
            val maxWaveNum = 0
            val archwingRequired = false
            @JsonProperty("isSharkwingMission")
            val isSharkwingMission = false
            val requiredItems = listOf<String>()
            val consumeRequiredItems = false
            val missionReward = MissionReward()
            val goalTag = ""
            val levelAuras = listOf<String>()
            val icon = ""
            val nightmare = false
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
        val productExpiryOverride = Date()
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
        @JsonDeserialize(using = MissionRewardDeserializer::class)
        val attackerReward = listOf<MissionReward>()
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
        val seed = 0L
        val hide = false
        val faction = ""
        val enemySpec = ""
        val extraEnemySpec = ""
        val activation = Date()
        val expiry = Date()
        val customNpcEncounters = listOf<String>()
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
        val manifest = listOf<Item>()

        class Item {
            val itemType = ""
            val primePrice = 0
            val regularPrice = 0
        }
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

    class PersistentEnemies {
        @JsonProperty("_id")
        val id = ID()
        val agentType = ""
        val locTag = ""
        val icon = ""
        val rank = 0
        val healthPercent = 0.0
        val fleeDamage = 0
        val region = 0
        val lastDiscoveredLocation = ""
        val lastDiscoveredTime = Date()
        val discovered = false
        val useTicketing = false
    }

    class TwitchPromo {
        val startDate = Date()
        val endDate = Date()
        val type = ""
        val streamers = listOf<Any>()
    }

    class Job {
        val jobType = ""
        val rewards = ""
        val masteryReq = 0
        val minEnemyLevel = 0
        val maxEnemyLevel = 0
        val xpAmounts = listOf<Int>()
    }

    class MissionReward {
        val credits = 0
        val items = listOf<String>()
        val countedItems = listOf<CountedItems>()
        val randomizedItems = ""

        class CountedItems {
            val itemType = ""
            val itemCount = 0
        }
    }

    class PrimeVaultAvailability {
        val state = ""
    }

    companion object {
        private const val WORLDSTATE_DATA_URL =
            "https://raw.githubusercontent.com/WFCD/warframe-worldstate-data/master/data"

        private val jsonMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }

        fun getArcaneInfo(arcane: String): Arcane {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/arcanes.json"))
                    .find { arcane.matches(it.get("regex").asText().toRegex()) }
                    ?.let {
                        jsonMapper.readValue<Arcane>(it.toString())
                    } ?: Arcane()
            } catch (e: Exception) {
                Arcane()
            }
        }

        fun getFactionString(faction: String): String {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/factionsData.json"))
                    .get(faction)
                    .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        fun getFissureModifier(tier: String): String {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/fissureModifiers.json"))
                    .get(tier)
                    .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        fun getLanguageFromAsset(encoded: String): String {
            val mapper = jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/languages.json"))
            return try {
                mapper.get(encoded).get("value").asText()
            } catch (e: Exception) {
                try {
                    mapper.get(encoded.toLowerCase()).get("value").asText()
                } catch (e: Exception) {
                    ""
                }
            }
        }

        fun getMissionType(missionType: String): String {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/missionTypes.json"))
                    .get(missionType)
                    .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        fun getSolNode(solNode: String): SolNode {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/solNodes.json"))
                    .get(solNode)
                    .let {
                        jsonMapper.readValue(it.toString())
                    }
            } catch (e: Exception) {
                SolNode()
            }
        }

        fun getSortieModifier(modifier: String): SortieModifier {
            return try {
                val tree = jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/sortieData.json"))

                SortieModifier(
                    tree.get("modifierTypes").get(modifier).asText(),
                    tree.get("modifierDescriptions").get(modifier).asText()
                )
            } catch (e: Exception) {
                SortieModifier("", "")
            }
        }

        fun getSortieBoss(boss: String): SortieBoss {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/sortieData.json"))
                    .get("bosses")
                    .get(boss)
                    .let {
                        jsonMapper.readValue(it.toString())
                    }
            } catch (e: Exception) {
                SortieBoss()
            }
        }

        fun getSyndicateName(syndicate: String): String {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/syndicatesData.json"))
                    .get(syndicate)
                    .get("name").asText()
            } catch (e: Exception) {
                ""
            }
        }

        fun getUpgradeType(upgrade: String): String {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/upgradeTypes.json"))
                    .get(upgrade)
                    .get("value").asText()
            } catch (e: Exception) {
                ""
            }
        }

        fun getWarframeInfo(warframe: String): Warframe {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/warframes.json"))
                    .find { warframe.matches(it.get("regex").asText().toRegex()) }
                    ?.let {
                        jsonMapper.readValue<Warframe>(it.toString())
                    } ?: Warframe()
            } catch (e: Exception) {
                Warframe()
            }
        }

        fun getWeaponInfo(weapon: String): Weapon {
            return try {
                jsonMapper.readTree(URL("$WORLDSTATE_DATA_URL/weapons.json"))
                    .find { weapon.matches(it.get("regex").asText().toRegex()) }
                    ?.let {
                        jsonMapper.readValue<Weapon>(it.toString())
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

        }
    }
}