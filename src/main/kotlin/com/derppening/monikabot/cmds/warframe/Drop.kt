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

package com.derppening.monikabot.cmds.warframe

import com.derppening.monikabot.cmds.IBase
import com.derppening.monikabot.cmds.Warframe
import com.derppening.monikabot.core.BuilderHelper.buildEmbed
import com.derppening.monikabot.core.BuilderHelper.buildMessage
import com.derppening.monikabot.core.BuilderHelper.insertSeparator
import com.derppening.monikabot.core.FuzzyMatcher
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.models.warframe.drop.DropInfo
import com.derppening.monikabot.models.warframe.droptable.BaseDrop
import com.derppening.monikabot.models.warframe.droptable.BaseEnemy
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.text.DecimalFormat
import kotlin.system.measureTimeMillis

object Drop : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        when {
            args.isEmpty() -> {
                help(event, false)
            }
            args[0].matches(Regex("(?:blueprint|bp)", RegexOption.IGNORE_CASE)) -> findBlueprint(args.drop(1), event)
//            args[0].matches(Regex("bount(?:y|ies)", RegexOption.IGNORE_CASE)) -> findBounty(args.drop(1), event)
            args[0].matches(Regex("enem(?:y|ies)", RegexOption.IGNORE_CASE)) -> findEnemy(args.drop(1), event)
            args[0].matches(Regex("ke(?:y|ies)", RegexOption.IGNORE_CASE)) -> findKey(args.drop(1), event)
            args[0].matches(Regex("mods?", RegexOption.IGNORE_CASE)) -> findMod(args.drop(1), event)
            args[0].matches(Regex("mission", RegexOption.IGNORE_CASE)) -> findMission(args.drop(1), event)
            args[0].matches(Regex("op(?:peration)?", RegexOption.IGNORE_CASE)) -> findOp(args.drop(1), event)
            args[0].matches(Regex("relics?", RegexOption.IGNORE_CASE)) -> findRelic(args.drop(1), event)
            args[0].matches(Regex("primes?", RegexOption.IGNORE_CASE)) -> findPrime(args.drop(1), event)
            args[0].matches(Regex("sorties?", RegexOption.IGNORE_CASE)) -> sortie(event)
            else -> {
                findAll(args, event)
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-drop`")
            withDesc("Displays drop chance information for different locations in-game.")
            insertSeparator()

            appendField("Usage", "```warframe drop [loc_type] [location]```", false)
            appendField("`[loc_type]`", "Specifies the type of location to search for." +
                    "\nRecognized categories include:" +
                    "\n- `blueprint`" +
//                    "\n- `bounty`" +
                    "\n- `enemy`" +
                    "\n- `key`" +
                    "\n- `mission`" +
                    "\n- `operation`" +
                    "\n- `relic`" +
                    "\n- `sortie`", false)
            appendField("`[location]", "The name of the location to lookup drop tables.", false)
            insertSeparator()

            appendField("Usage", "```warframe drop [item_type] [location]```", false)
            appendField("`[item_type]`", "If give, specifies the category of item to search for." +
                    "\nRecognized categories include:" +
                    "\n- `mod`" +
                    "\n- `prime`", false)
            appendField("`[item]`", "Item to search drop locations for.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    private fun findAll(args: List<String>, event: MessageReceivedEvent) {
        val drop = FuzzyMatcher(args, allDrops.map { it.name }) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let { name ->
            if (name.isBlank()) {
                return
            } else {
                allDrops.first { name == it.name }
            }
        }

        buildEmbed(event.channel) {
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
        }
    }

    private fun findBlueprint(args: List<String>, event: MessageReceivedEvent) {
        val mod = FuzzyMatcher(args, Warframe.dropTables.blueprintLocations.map { it.blueprintName }) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let { modName ->
            if (modName.isBlank()) {
                return
            } else {
                Warframe.dropTables.blueprintLocations.first { modName == it.blueprintName }
            }
        }

        buildEmbed(event.channel) {
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
        }
    }

    private fun findEnemy(args: List<String>, event: MessageReceivedEvent) {
        val allEnemyMap = Warframe.dropTables.enemyModTables.map { it.enemyName }.union(Warframe.dropTables.enemyBlueprintTables.map { it.enemyName })

        val enemyName = FuzzyMatcher(args, allEnemyMap.toList()) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let { modName ->
            if (modName.isBlank()) {
                return
            } else {
                modName
            }
        }

        val modTable = Warframe.dropTables.enemyModTables.firstOrNull { it.enemyName == enemyName }
        val bpTable = Warframe.dropTables.enemyBlueprintTables.firstOrNull { it.enemyName == enemyName }

        buildEmbed(event.channel) {
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
        }
    }

    private fun findKey(args: List<String>, event: MessageReceivedEvent) {
        val key = FuzzyMatcher(args, Warframe.dropTables.keyRewards.map { it.keyName }) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let { keyName ->
            if (keyName.isBlank()) {
                return
            } else {
                Warframe.dropTables.keyRewards.first { keyName == it.keyName }
            }
        }

        buildEmbed(event.channel) {
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
        }
    }

    private fun findMission(args: List<String>, event: MessageReceivedEvent) {
        val mission = FuzzyMatcher(args, Warframe.dropTables.missionRewards.flatMap { it.nodes.map { it.name } }) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let { missionName ->
            if (missionName.isBlank()) {
                return
            } else {
                Warframe.dropTables.missionRewards.let {
                    "$missionName (${it.first { it.nodes.any { it.name == missionName }}.name})" to
                            it.flatMap { it.nodes }.first { it.name == missionName }
                }
            }
        }

        buildEmbed(event.channel) {
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
        }
    }

    private fun findMod(args: List<String>, event: MessageReceivedEvent) {
        val mod = FuzzyMatcher(args, Warframe.dropTables.modLocations.map { it.modName }) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let { modName ->
            if (modName.isBlank()) {
                return
            } else {
                Warframe.dropTables.modLocations.first { modName == it.modName }
            }
        }

        buildEmbed(event.channel) {
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
        }
    }

    private fun findOp(args: List<String>, event: MessageReceivedEvent) {
        val op = FuzzyMatcher(args, Warframe.dropTables.transientRewards.map { it.objectiveName }) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let { opName ->
            if (opName.isBlank()) {
                return
            } else {
                Warframe.dropTables.transientRewards.first { it.objectiveName == opName }
            }
        }

        buildEmbed(event.channel) {
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
        }
    }

    private fun findPrime(args: List<String>, event: MessageReceivedEvent) {
        val item = FuzzyMatcher(args, relicDrops) {
            emptyMatchMessage {
                "Cannot find item with given search!" to event.channel
            }
            multipleMatchMessage {
                "Multiple items match your given search! Including:\n\n{5(\n)}\n\n... With a total of {size} results." to event.channel
            }
            regex(RegexOption.IGNORE_CASE)
        }.matchOne().let {
            if (it.isBlank()) {
                return
            } else {
                it
            }
        }

        val relics = Warframe.dropTables.relics
                .filter { it.state == "Flawless" }
                .filter { it.rewards.any { it.itemName == item } }

        if (relics.isEmpty()) {
            buildMessage(event.channel) {
                withContent("Cannot find $item in relic drop tables!")
            }
        } else {
            buildEmbed(event.channel) {
                withTitle(item)

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
        }
    }

    private fun findRelic(args: List<String>, event: MessageReceivedEvent) {
        if (args.size !in 2..3 && args.getOrNull(2)?.equals("relic", true) != false) {
            buildMessage(event.channel) {
                withContent("Please enter the relic with format \"[Tier] [Relic]\"!")
            }
            return
        }

        val (tier, relicName) = args

        val relic = Warframe.dropTables.relics.find {
            it.tier.startsWith(tier, true) &&
                    it.relicName.equals(relicName, true) &&
                    it.state == "Flawless"
        } ?: run {
            buildMessage(event.channel) {
                withContent("Cannot find such a relic!")
            }
            return
        }

        buildEmbed(event.channel) {
            withTitle("${relic.tier} ${relic.relicName} Relic")

            val common = relic.rewards.filter { it.chance == 20.0 }
            val uncommon = relic.rewards.filter { it.chance == 17.0 }
            val rare = relic.rewards.filter { it.chance == 6.0 }

            appendField("Common Drops", common.joinToString("\n") { it.itemName }, false)
            appendField("Uncommon Drops", uncommon.joinToString("\n") { it.itemName }, false)
            appendField("Rare Drops", rare.joinToString("\n") { it.itemName }, false)
        }
    }

    private fun sortie(event: MessageReceivedEvent) {
        val sortieDropTable = Warframe.dropTables.sortieRewards

        buildEmbed(event.channel) {
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
        }
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
