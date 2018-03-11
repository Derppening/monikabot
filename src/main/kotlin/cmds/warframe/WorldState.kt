package cmds.warframe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
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
    val alerts = listOf<Alert>()
    val sorties = listOf<Sorties>()
    val syndicateMissions = listOf<SyndicateMission>()
    val activeMissions = listOf<ActiveMission>()
    //    val globalUpgrades = listOf<>()
    val flashSales = listOf<FlashSale>()
    val invasions = listOf<Invasion>()
    //    val hubEvents = listOf<>()
    val nodeOverrides = listOf<NodeOverride>()
    val voidTraders = listOf<VoidTrader>()
    val primeAccessAvailability = PrimeVaultAvailability()
    val primeAccessAvailabilities = listOf<PrimeVaultAvailability>()
    val dailyDeals = listOf<DailyDeal>()
    val libraryInfo = LibraryInfo()
    val pvpChallengeInstances = listOf<PVPChallengeInstance>()
//    val persistentEnemies = listOf<>()
//    val pvpAlternativeMode = listOf<>()
//    val pvpActiveTournaments = listOf<>()
    val projectPct = listOf<Double>()
//    val constructionProjects = listOf<>()
//    val twitchPromos = listOf<>()

    class Event {
        @JsonProperty("_id")
        val id = ID()
        val messages = listOf<Message>()
        val prop = ""
        val date = Date()
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
            val minEnemyLevel = 0
            val maxEnemyLevel = 0
            val difficulty = 0.0
            val seed = 0
            val maxWaveNum = 0
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
        //        val extraDrops = listOf<String>()
        val seed = 0
        val variant = Variant()
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

        class MissionInfo {
            val seed = 0
            val faction = ""
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
        val isGenerate = false
        val PVPMode = ""
        val subChallenges = listOf<ID>()
        val category = ""

        class Param {
            val n = ""
            val p = 0
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
}