package cmds.warframe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant

class DropTable {
    @JsonDeserialize(using = PlanetDeserializer::class)
    val missionRewards = setOf<Planet>()
    val relics = listOf<Relic>()
    val transientRewards = listOf<TransientReward>()
    val modLocations = listOf<ModLocation>()
    val enemyModTables = listOf<EnemyModTable>()
    val blueprintLocations = listOf<BlueprintLocation>()
    val enemyBlueprintTables = listOf<EnemyBlueprintTable>()
    val sortieRewards = listOf<MissionDropInfo.Drop>()
    val keyRewards = listOf<KeyReward>()
    val cetusBountyRewards = listOf<CetusBountyReward>()

    class BlueprintLocation {
        @JsonProperty("_id")
        val id = ""
        val blueprintName = ""
        val enemies = listOf<Enemy>()

        class Enemy {
            @JsonProperty("_id")
            val id = ""
            val enemyName = ""
            val enemyBlueprintDropChance = 0.0
            val rarity = ""
            val chance = 0.0
        }
    }

    class CetusBountyReward {
        @JsonProperty("_id")
        val id = ""
        val bountyLevel = ""
        val rewards = Rewards()

        data class Rewards(val a: List<Drop> = listOf(),
                           val b: List<Drop> = listOf(),
                           val c: List<Drop> = listOf())

        class Drop {
            @JsonProperty("_id")
            val id = ""
            val itemName = ""
            val rarity = ""
            val chance = 0.0
            val stage = ""
        }
    }

    class EnemyBlueprintTable {
        @JsonProperty("_id")
        val id = ""
        val enemyName = ""
        val blueprintDropChance = 0.0
        val mods = listOf<Mod>()

        class Mod {
            @JsonProperty("_id")
            val id = ""
            val modName = ""
            val rarity = ""
            val chance = 0.0
        }
    }

    class EnemyModTable {
        @JsonProperty("_id")
        val id = ""
        val enemyName = ""
        val ememyModDropChance = 0.0
        val mods = listOf<Mod>()

        class Mod {
            @JsonProperty("_id")
            val id = ""
            val modName = ""
            val rarity = ""
            val chance = 0.0
        }
    }

    class KeyReward {
        @JsonProperty("_id")
        val id = ""
        val keyName = ""
        val rewards = Rewards()

        data class Rewards(val a: List<Drop> = listOf(),
                           val b: List<Drop> = listOf(),
                           val c: List<Drop> = listOf())

        class Drop {
            @JsonProperty("_id")
            val id = ""
            val itemName = ""
            val rarity = ""
            val chance = 0.0
        }
    }

    class MissionDropInfo {
        val gameMode = ""
        @JsonProperty("isEvent")
        val isEvent = false
        @JsonDeserialize(using = RewardDeserializer::class)
        val rewards = Rewards()

        data class Rewards(val a: List<Drop> = listOf(),
                           val b: List<Drop> = listOf(),
                           val c: List<Drop> = listOf())

        class Drop {
            @JsonProperty("_id")
            val id = ""
            val itemName = ""
            val rarity = ""
            val chance = 0.0
        }

        class RewardDeserializer : JsonDeserializer<Rewards>() {
            override fun deserialize(parser: JsonParser, context: DeserializationContext?): Rewards {
                return if (parser.currentToken == JsonToken.START_ARRAY) {
                    val mapper = ObjectMapper().apply {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    }.readValue<List<Drop>>(parser.readValueAsTree<JsonNode>().toString())

                    Rewards(a = mapper)
                } else {
                    ObjectMapper().apply {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    }.readValue(parser.readValueAsTree<JsonNode>().toString())
                }
            }
        }
    }

    class ModLocation {
        @JsonProperty("_id")
        val id = ""
        val modName = ""
        val enemies = listOf<Enemy>()

        class Enemy {
            @JsonProperty("_id")
            val id = ""
            val enemyName = ""
            val enemyModDropChance = 0.0
            val rarity = ""
            val chance = 0.0
        }
    }

    data class Planet(val name: String = "",
                      val nodes: Set<Node> = setOf()){
        data class Node(val name: String = "",
                        val drops: DropTable.MissionDropInfo = DropTable.MissionDropInfo())
    }

    class Relic {
        val tier = ""
        val relicName = ""
        val state = ""
        val rewards = listOf<Drop>()
        @JsonProperty("_id")
        val id = ""

        class Drop {
            @JsonProperty("_id")
            val id = ""
            val itemName = ""
            val rarity = ""
            val chance = 0.0
        }
    }

    class TransientReward {
        val objectiveName = ""
        val rewards = listOf<Reward>()

        class Reward {
            @JsonProperty("_id")
            val id = ""
            val rotation = ""
            val itemName = ""
            val rarity = ""
            val chance = 0.0
        }
    }

    class Info {
        val hash = ""
        @JsonDeserialize(using = TimeMillisDeserializer::class)
        val timestamp = Instant.EPOCH

        class TimeMillisDeserializer : JsonDeserializer<Instant>() {
            override fun deserialize(parser: JsonParser, context: DeserializationContext?): Instant {
                return Instant.ofEpochMilli(parser.valueAsLong)
            }
        }
    }

    class PlanetDeserializer : JsonDeserializer<Set<Planet>>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext?): Set<Planet> {
            val planetTree = parser.codec.readTree<JsonNode>(parser)
            val planetSet = mutableSetOf<Planet>()
            val planetFields = planetTree.fieldNames()

            planetTree.forEach {
                val planetName = planetFields.next()

                val nodes = mutableSetOf<Planet.Node>()
                val nodeFields = planetTree[planetName].fieldNames()
                planetTree[planetName].forEach {
                    val nodeName = nodeFields.next()
                    val drops = ObjectMapper().apply {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    }.readValue<DropTable.MissionDropInfo>(it.toString())

                    nodes.add(Planet.Node(nodeName, drops))
                }

                planetSet.add(Planet(planetName, nodes))
            }

            return planetSet
        }
    }
}
