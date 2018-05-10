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

package com.derppening.monikabot.commands

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.DogService
import com.derppening.monikabot.impl.DogService.getBreed
import com.derppening.monikabot.impl.DogService.getRandomPic
import com.derppening.monikabot.impl.DogService.getSubbreed
import com.derppening.monikabot.impl.DogService.list
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.insertSeparator
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Dog : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).dropWhile { it == "--experimental" }

        when {
            args.isEmpty() -> {
                buildEmbed(event.channel) {
                    fields {
                        withImage(getRandomPic())
                    }
                }
            }
            args[0].matches(Regex("-{0,2}list")) -> list(args, event)
            args.isNotEmpty() -> showBreed(args, event)
        }

        return Parser.HandleState.HANDLED
    }

    private fun list(args: List<String>, event: MessageReceivedEvent) {
        list(args.getOrNull(1) ?: "").also {
            when (it) {
                is DogService.ListResult.Success -> {
                    event.channel.sendMessage(it.embed)
                }
                is DogService.ListResult.Failure -> {
                    buildMessage(event.channel) {
                        content {
                            withContent(it.message)
                        }
                    }
                }
            }
        }
    }

    private fun showBreed(args: List<String>, event: MessageReceivedEvent) {
        event.channel.toggleTypingStatus()

        if (args.size != 1) {
            showSubbreed(args, event)
        } else {
            getBreed(args[0]).also {
                when (it) {
                    is DogService.ShowResult.Success -> {
                        buildEmbed(event.channel) { fields { withImage(it.link) } }
                    }
                    is DogService.ShowResult.Failure -> {
                        buildMessage(event.channel) { content { withContent(it.message) } }
                    }
                }
            }
        }
    }

    private fun showSubbreed(args: List<String>, event: MessageReceivedEvent) {
        event.channel.toggleTypingStatus()

        getSubbreed(args[0], args[1]).also {
            when (it) {
                is DogService.ShowResult.Success -> {
                    buildEmbed(event.channel) { fields { withImage(it.link) } }
                }
                is DogService.ShowResult.Failure -> {
                    buildMessage(event.channel) { content { withContent(it.message) } }
                }
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            fields {
                withTitle("Help Text for `dog`")
                withDesc("Displays a photo of a dog.")
                insertSeparator()
                appendField("Usage", "```dog [breed] [subbreed]```", false)
                appendField("`[breed]", "If specified, only display images of the given breed.", false)
                appendField("`[subbreed]`", "If specified, only display images of the given subbreed. This option must be used in conjunction with `[breed]`.", false)
                insertSeparator()
                appendField("Usage", "```dog --list [breed]```", false)
                appendField("`--list`", "Lists all breeds of dogs that can be retrieved from this command.", false)
                appendField("`[breed]", "If specified, lists all subbreeds of the given breed.", false)
            }

            onError {
                discordException { e ->
                    log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                        author { event.author }
                        channel { event.channel }
                        info { e.errorMessage }
                    }
                }
            }
        }
    }
}