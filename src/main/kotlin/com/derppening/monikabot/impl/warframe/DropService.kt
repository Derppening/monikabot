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

package com.derppening.monikabot.impl.warframe

import com.derppening.monikabot.cmds.Warframe
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.models.warframe.drop.DropInfo
import com.derppening.monikabot.models.warframe.droptable.BaseDrop
import com.derppening.monikabot.models.warframe.droptable.BaseEnemy
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import com.derppening.monikabot.util.FuzzyMatcher
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.text.DecimalFormat
import kotlin.system.measureTimeMillis

object DropService : ILogger {
    fun findAll(args: List<String>): FindResult {
        val drop = FuzzyMatcher(args, allDrops.map { it.name }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first().let { name ->
            allDrops.first { name == it.name }
        }

        return EmbedBuilder().apply {
            withTitle(drop.name)

            val displayLocs = drop.locs.sortedByDescending { it.chance }
                    .joinToString("\n") {
                        val chance = DecimalFormat("0.###%").format(it.chance)
                        "${it.loc}: $chance"
                    }.let {
                        it.take(2048).dropLastWhile { it != '\n' && it != '%' }.dropLastWhile { it == '\n' }.also {
                            withDesc(it)
                        }
                    }.split("\n")

            withFooterText("Showing ${displayLocs.size} of ${drop.locs.size} locations.")
        }.build().let { FindResult.Success(it) }
    }

    fun findBlueprint(args: List<String>): FindResult {
        val mod = FuzzyMatcher(args, Warframe.dropTables.blueprintLocations.map { it.blueprintName }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first().let { modName ->
            Warframe.dropTables.blueprintLocations.first { modName == it.blueprintName }
        }

        return EmbedBuilder().apply {
            withTitle(mod.blueprintName)

            rarity.forEach { rarity ->
                mod.enemies.filter { it.rarity.equals(rarity, true) }.let {
                    if (it.isNotEmpty()) {
                        appendField("$rarity Locations",
                                it.joinToString("\n") {
                                    val chance = DecimalFormat("0.###%").format(it.chance * it.enemyBlueprintDropChance / 100)
                                    "${it.enemyName} - $chance"
                                },
                                false)
                    }
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    fun findEnemy(args: List<String>): FindResult {
        val allEnemyMap = Warframe.dropTables.enemyModTables.map { it.enemyName }.union(Warframe.dropTables.enemyBlueprintTables.map { it.enemyName })

        val (modTable, bpTable) = FuzzyMatcher(args, allEnemyMap.toList()) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first().let { enemyName ->
            Warframe.dropTables.enemyModTables.firstOrNull { it.enemyName == enemyName } to
                    Warframe.dropTables.enemyBlueprintTables.firstOrNull { it.enemyName == enemyName }
        }

        return EmbedBuilder().apply {
            withTitle(modTable?.enemyName ?: bpTable?.enemyName)

            if (modTable != null) {
                withDesc("Mod Drop Chance: ${modTable.ememyModDropChance}%")

                rarity.forEach { rarity ->
                    modTable.mods.filter { it.rarity.equals(rarity, true) }.let {
                        if (it.isNotEmpty()) {
                            appendField("$rarity Mod Drops",
                                    it.joinToString("\n") {
                                        val chance = DecimalFormat("0.###%").format(it.chance / 10000)
                                        "${it.modName} - $chance"
                                    },
                                    false)
                        }
                    }
                }
            }

            if (bpTable != null) {
                insertSeparator()

                appendDesc("\nBlueprint Drop Chance: ${bpTable.blueprintDropChance}%")
                rarity.forEach { rarity ->
                    bpTable.mods.filter { it.rarity.equals(rarity, true) }.let {
                        if (it.isNotEmpty()) {
                            appendField("$rarity Blueprint Drops",
                                    it.joinToString("\n") {
                                        val chance = DecimalFormat("0.###%").format(it.chance / 10000)
                                        "${it.modName} - $chance"
                                    },
                                    false)
                        }
                    }
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    fun findKey(args: List<String>): FindResult {
        val key = FuzzyMatcher(args, Warframe.dropTables.keyRewards.map { it.keyName }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first().let { keyName ->
            Warframe.dropTables.keyRewards.first { keyName == it.keyName }
        }

        return EmbedBuilder().apply {
            withTitle(key.keyName)

            val fmt: (List<BaseDrop.RewardDrop>) -> String = {
                it.sortedByDescending { it.chance }.joinToString("\n") { "${it.itemName}: ${it.chance}%" }
            }

            key.rewards.also {
                it.a.also {
                    appendField("Rotation A", fmt(it), false)
                }
                it.b.also {
                    appendField("Rotation B", fmt(it), false)
                }
                it.c.also {
                    appendField("Rotation C", fmt(it), false)
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    fun findMission(args: List<String>): FindResult {
        val mission = FuzzyMatcher(args, Warframe.dropTables.missionRewards.flatMap { it.nodes.map { it.name } }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first().let { missionName ->
            Warframe.dropTables.missionRewards.let {
                "$missionName (${it.first { it.nodes.any { it.name == missionName } }.name})" to
                        it.flatMap { it.nodes }.first { it.name == missionName }
            }
        }

        return EmbedBuilder().apply {
            withTitle(mission.first)

            val fmt: (List<BaseDrop.RewardDrop>) -> String = {
                it.sortedByDescending { it.chance }.joinToString("\n") { "${it.itemName}: ${it.chance}%" }
            }

            mission.second.drops.rewards.also {
                it.a.also {
                    appendField("Rotation A", fmt(it), false)
                }
                it.b.also {
                    appendField("Rotation B", fmt(it), false)
                }
                it.c.also {
                    appendField("Rotation C", fmt(it), false)
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    fun findMod(args: List<String>): FindResult {
        val mod = FuzzyMatcher(args, Warframe.dropTables.modLocations.map { it.modName }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first().let { modName ->
            Warframe.dropTables.modLocations.first { modName == it.modName }
        }

        return EmbedBuilder().apply {
            withTitle(mod.modName)

            rarity.forEach { rarity ->
                mod.enemies.filter { it.rarity.equals(rarity, true) }.let {
                    if (it.isNotEmpty()) {
                        appendField("$rarity Locations",
                                it.joinToString("\n") {
                                    val chance = DecimalFormat("0.###%").format(it.chance * it.enemyModDropChance / 10000)
                                    "${it.enemyName} - $chance"
                                },
                                false)
                    }
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    fun findOp(args: List<String>): FindResult {
        val op = FuzzyMatcher(args, Warframe.dropTables.transientRewards.map { it.objectiveName }) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first().let { opName ->
            Warframe.dropTables.transientRewards.first { it.objectiveName == opName }
        }

        return EmbedBuilder().apply {
            withTitle(op.objectiveName)

            rarity.forEach { rarity ->
                op.rewards.filter { it.rarity.equals(rarity, true) }.let {
                    if (it.isNotEmpty()) {
                        appendField("$rarity Blueprint Drops",
                                it.joinToString("\n") {
                                    val str = if (it.rotation != "null" && it.rotation != null) {
                                        "[Rotation ${it.rotation}] "
                                    } else {
                                        ""
                                    }
                                    val chance = DecimalFormat("0.###%").format(it.chance / 100)
                                    "$str${it.itemName} - $chance"
                                },
                                false)
                    }
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    fun findPrime(args: List<String>): FindResult {
        val item = FuzzyMatcher(args, relicDrops) {
            regex(RegexOption.IGNORE_CASE)
        }.matches().also {
            if (it.size != 1) {
                return FindResult.Failure(it)
            }
        }.first()

        val relics = item.let { itemName ->
            Warframe.dropTables.relics
                    .filter { it.state == "Flawless" }
                    .filter { it.rewards.any { it.itemName == itemName }}
        }

        return EmbedBuilder().apply {
            withTitle(item)

            if (relics.isEmpty()) {
                withDesc("Cannot find $item in relic drop tables!")
            } else {
                relicTiers.forEach { tier ->
                    relics.filter { it.tier == tier }.also {
                        if (it.isNotEmpty()) {
                            appendField(tier, it.joinToString("\n") {
                                "${it.relicName} - ${flawlessRarityConverter(it.rewards.first { it.itemName.equals(item, true) }.chance)}"
                            }, false)
                        }
                    }
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    fun findRelic(args: List<String>): FindResult {
        val (tier, relicName) = args

        val relic = Warframe.dropTables.relics.find {
            it.tier.startsWith(tier, true) &&
                    it.relicName.equals(relicName, true) &&
                    it.state == "Flawless"
        } ?: run {
            return FindResult.Failure(emptyList())
        }

        return EmbedBuilder().apply {
            withTitle("${relic.tier} ${relic.relicName} Relic")

            val common = relic.rewards.filter { it.chance == 20.0 }
            val uncommon = relic.rewards.filter { it.chance == 17.0 }
            val rare = relic.rewards.filter { it.chance == 6.0 }

            appendField("Common Drops", common.joinToString("\n") { it.itemName }, false)
            appendField("Uncommon Drops", uncommon.joinToString("\n") { it.itemName }, false)
            appendField("Rare Drops", rare.joinToString("\n") { it.itemName }, false)
        }.build().let { FindResult.Success(it) }
    }

    fun sortie(args: List<String> = emptyList()): FindResult {
        val sortieDropTable = Warframe.dropTables.sortieRewards

        return EmbedBuilder().apply {
            withTitle("Sorties Drop Table")

            rarity.forEach { rarity ->
                sortieDropTable.filter { it.rarity.equals(rarity, true) }.sortedByDescending { it.chance }.let {
                    if (it.isNotEmpty()) {
                        appendField("$rarity Rewards",
                                it.joinToString("\n") {
                                    "${it.itemName} - ${it.chance}%"
                                },
                                false)
                    }
                }
            }
        }.build().let { FindResult.Success(it) }
    }

    private fun flatMapDropTable() {
        measureTimeMillis {
            allDrops.clear()

            val parseReward: (BaseDrop, String) -> Unit = { drop, loc ->
                val info = DropInfo.DropLocation(loc, drop.chance / 100)
                when (drop) {
                    is BaseDrop.ModDrop -> {
                        allDrops.firstOrNull { it.name == drop.modName }?.locs?.add(info)
                                ?: allDrops.add(DropInfo(drop.modName, mutableListOf(info)))
                    }
                    is BaseDrop.RewardDrop -> {
                        allDrops.firstOrNull { it.name == drop.itemName }?.locs?.add(info)
                                ?: allDrops.add(DropInfo(drop.itemName, mutableListOf(info)))
                    }
                }
            }

            val parseEnemy: (String, BaseEnemy) -> Unit = { name, enemy ->
                val info = when (enemy) {
                    is BaseEnemy.EnemyMod -> DropInfo.DropLocation(enemy.enemyName, enemy.enemyModDropChance * enemy.chance / 10000)
                    is BaseEnemy.EnemyBlueprint -> DropInfo.DropLocation(enemy.enemyName, enemy.enemyBlueprintDropChance * enemy.chance / 10000)
                }
                allDrops.firstOrNull { it.name == name }?.locs?.add(info)
                        ?: allDrops.add(DropInfo(name, mutableListOf(info)))
            }

            Warframe.dropTables.missionRewards.forEach { planet ->
                planet.nodes.forEach { node ->
                    val rot: (String) -> String = {
                        "${node.name} (${planet.name}) - Rotation $it"
                    }

                    node.drops.rewards.a.forEach { parseReward(it, rot("A")) }
                    node.drops.rewards.b.forEach { parseReward(it, rot("B")) }
                    node.drops.rewards.c.forEach { parseReward(it, rot("C")) }
                }
            }

            Warframe.dropTables.relics.filter { it.state == "Intact" }.forEach { relic ->
                relic.rewards.forEach {
                    parseReward(it, "${relic.tier} ${relic.relicName} Relic")
                }
            }

            Warframe.dropTables.transientRewards.forEach { transient ->
                transient.rewards.forEach {
                    parseReward(it, "${transient.objectiveName} - Rotation ${it.rotation}")
                }
            }

            Warframe.dropTables.modLocations.forEach { mod ->
                mod.enemies.forEach {
                    parseEnemy(mod.modName, it)
                }
            }

            Warframe.dropTables.blueprintLocations.forEach { bp ->
                bp.enemies.forEach {
                    parseEnemy(bp.blueprintName, it)
                }
            }

            Warframe.dropTables.sortieRewards.forEach {
                parseReward(it, "Sorties")
            }

            Warframe.dropTables.keyRewards.forEach { key ->
                val rot: (String) -> String = {
                    "${key.keyName} - Rotation $it"
                }

                key.rewards.a.forEach { parseReward(it, rot("A")) }
                key.rewards.b.forEach { parseReward(it, rot("B")) }
                key.rewards.c.forEach { parseReward(it, rot("C")) }
            }

            Warframe.dropTables.cetusBountyRewards.forEach { bounty ->
                val rot: (String) -> String = {
                    "${bounty.bountyLevel} - Rotation $it"
                }

                bounty.rewards.a.forEach { parseReward(it, rot("A")) }
                bounty.rewards.b.forEach { parseReward(it, rot("B")) }
                bounty.rewards.c.forEach { parseReward(it, rot("C")) }
            }
        }.also { logger.debug("flatMapDropTable(): Mapping took $it ms") }
    }

    fun doCacheUpdate() {
        relicDrops = Warframe.dropTables.relics.flatMap { it.rewards.map { it.itemName } }.distinct()
        flatMapDropTable()
    }

    private fun flawlessRarityConverter(chance: Double): String =
            when (chance) {
                20.0 -> "Common"
                17.0 -> "Uncommon"
                6.0 -> "Rare"
                else -> "Unknown"
            }

    sealed class FindResult {
        class Success(val match: EmbedObject) : FindResult()
        class Failure(val matches: List<String>) : FindResult()
    }

    private val rarity = listOf(
            "Common",
            "Uncommon",
            "Rare",
            "Legendary"
    )

    private val relicTiers = listOf(
            "Lith",
            "Meso",
            "Neo",
            "Axi"
    )

    private var relicDrops = emptyList<String>()

    private val allDrops = mutableListOf<DropInfo>()
}