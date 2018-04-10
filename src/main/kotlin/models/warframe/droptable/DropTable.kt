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

package models.warframe.droptable

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import models.warframe.util.DropRewardDeserializer
import models.warframe.util.PlanetDeserializer
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
    val sortieRewards = listOf<BaseDrop.RewardDrop>()
    val keyRewards = listOf<KeyReward>()
    val cetusBountyRewards = listOf<CetusBountyReward>()

    class BlueprintLocation {
        @JsonProperty("_id")
        val id = ""
        val blueprintName = ""
        val enemies = listOf<BaseEnemy.EnemyBlueprint>()
    }

    class CetusBountyReward {
        @JsonProperty("_id")
        val id = ""
        val bountyLevel = ""
        val rewards = Rewards()

        data class Rewards(val a: List<Reward> = listOf(),
                           val b: List<Reward> = listOf(),
                           val c: List<Reward> = listOf())

        class Reward : BaseDrop.RewardDrop() {
            val stage = ""
        }
    }

    class EnemyBlueprintTable {
        @JsonProperty("_id")
        val id = ""
        val enemyName = ""
        val blueprintDropChance = 0.0
        val mods = listOf<BaseDrop.ModDrop>()
    }

    class EnemyModTable {
        @JsonProperty("_id")
        val id = ""
        val enemyName = ""
        val ememyModDropChance = 0.0
        val mods = listOf<BaseDrop.ModDrop>()
    }

    class KeyReward {
        @JsonProperty("_id")
        val id = ""
        val keyName = ""
        val rewards = Rewards()

        data class Rewards(val a: List<BaseDrop.RewardDrop> = listOf(),
                           val b: List<BaseDrop.RewardDrop> = listOf(),
                           val c: List<BaseDrop.RewardDrop> = listOf())
    }

    class MissionDropInfo {
        val gameMode = ""
        @JsonProperty("isEvent")
        val isEvent = false
        @JsonDeserialize(using = DropRewardDeserializer::class)
        val rewards = Rewards()

        data class Rewards(val a: List<BaseDrop.RewardDrop> = listOf(),
                           val b: List<BaseDrop.RewardDrop> = listOf(),
                           val c: List<BaseDrop.RewardDrop> = listOf())

    }

    class ModLocation {
        @JsonProperty("_id")
        val id = ""
        val modName = ""
        val enemies = listOf<BaseEnemy.EnemyMod>()
    }

    data class Planet(val name: String = "",
                      val nodes: Set<Node> = setOf()){
        data class Node(val name: String = "",
                        val drops: MissionDropInfo = MissionDropInfo())
    }

    class Relic {
        val tier = ""
        val relicName = ""
        val state = ""
        val rewards = listOf<BaseDrop.RewardDrop>()
        @JsonProperty("_id")
        val id = ""
    }

    class TransientReward {
        val objectiveName = ""
        val rewards = listOf<Reward>()

        class Reward : BaseDrop.RewardDrop() {
            val rotation = ""
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

}
