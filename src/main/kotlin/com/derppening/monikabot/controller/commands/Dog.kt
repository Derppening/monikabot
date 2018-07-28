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

package com.derppening.monikabot.controller.commands

import com.derppening.monikabot.controller.CommandInterpreter
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.DogService
import com.derppening.monikabot.impl.DogService.getBreed
import com.derppening.monikabot.impl.DogService.getRandomPic
import com.derppening.monikabot.impl.DogService.getSubbreed
import com.derppening.monikabot.impl.DogService.list
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Dog : IBase, ILogger {
    override fun cmdName(): String = "dog"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
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

        return CommandInterpreter.HandleState.HANDLED
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
        event.channel.typingStatus = true

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
        event.channel.typingStatus = true

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
        buildHelpText("dog", event) {
            description { "Displays a photo of a dog." }

            usage("dog [breed] [subbreed]") {
                def("[breed]") { "If specified, only display images of the given breed." }
                def("[subbreed]") { "If specified, only display images of the given subbreed. This option must be used in conjunction with `[breed]`." }
            }
            usage("dog --list [breed]") {
                def("--list") { "Lists all breeds of dogs that can be retrieved from this command." }
                def("[breed]") { "If specified, lists all subbreeds of the given breed." }
            }
        }
    }
}