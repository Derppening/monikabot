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

package com.derppening.monikabot.commands.warframe

import com.derppening.monikabot.commands.IBase
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.warframe.DropService
import com.derppening.monikabot.impl.warframe.DropService.FindResult
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel

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
        serviceDelegator(args, event.channel, DropService::findAll)
    }

    private fun findBlueprint(args: List<String>, event: MessageReceivedEvent) {
        serviceDelegator(args, event.channel, DropService::findBlueprint)
    }

    private fun findEnemy(args: List<String>, event: MessageReceivedEvent) {
        serviceDelegator(args, event.channel, DropService::findEnemy)
    }

    private fun findKey(args: List<String>, event: MessageReceivedEvent) {
        serviceDelegator(args, event.channel, DropService::findKey)
    }

    private fun findMission(args: List<String>, event: MessageReceivedEvent) {
        serviceDelegator(args, event.channel, DropService::findMission)
    }

    private fun findMod(args: List<String>, event: MessageReceivedEvent) {
        serviceDelegator(args, event.channel, DropService::findMod)
    }

    private fun findOp(args: List<String>, event: MessageReceivedEvent) {
        serviceDelegator(args, event.channel, DropService::findOp)
    }

    private fun findPrime(args: List<String>, event: MessageReceivedEvent) {
        serviceDelegator(args, event.channel, DropService::findPrime)
    }

    private fun findRelic(args: List<String>, event: MessageReceivedEvent) {
        if (args.size !in 2..3 && args.getOrNull(2)?.equals("relic", true) != false) {
            buildMessage(event.channel) {
                withContent("Please enter the relic with format \"[Tier] [Relic]\"!")
            }
            return
        }

        serviceDelegator(args, event.channel, DropService::findRelic)
    }

    private fun sortie(event: MessageReceivedEvent) {
        serviceDelegator(emptyList(), event.channel, DropService::sortie)
    }

    private fun serviceDelegator(args: List<String>, channel: IChannel, func: (List<String>) -> FindResult) {
        func(args).also {
            when (it) {
                is FindResult.Success -> channel.sendMessage(it.match)
                is FindResult.Failure -> failureMessage(it, channel)
            }
        }
    }

    private fun failureMessage(it: FindResult.Failure, channel: IChannel) {
        buildMessage(channel) {
            if (it.matches.isEmpty()) {
                withContent("Cannot find matching results with given search!")
            } else {
                withContent("Multiple results match your given search! Including:" +
                        "\n\n${it.matches.take(5).joinToString("\n")}" +
                        "\n\n... With a total of ${it.matches.size} results.")
            }
        }
    }
}
