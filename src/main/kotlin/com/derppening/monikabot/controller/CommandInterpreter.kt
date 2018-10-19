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

package com.derppening.monikabot.controller

import com.derppening.monikabot.controller.commands.*
import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.TriviaService
import com.derppening.monikabot.util.*
import com.derppening.monikabot.util.helpers.MessageHelper
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import java.io.File

object CommandInterpreter : ILogger {
    private var nullResponses = loadNullResponses()

    private const val NULL_RESPONSE_PATH = "lang/NullResponse.txt"

    private val commands: Map<String, IBase> = mapOf(
        "changelog" to Changelog,
        "clear" to Clear,
        "config" to Config,
        "debug" to Debug,
        "dog" to Dog,
        "echo" to Echo,
        "help" to Help,
        "issue" to Issue,
        "metar" to METAR,
        "ping" to Ping,
        "random" to Random,
        "reload" to Reload,
        "reminder" to Reminder,
        "rng" to RNG,
        "status" to Status,
        "stop" to Stop,
        "taf" to TAF,
        "timer" to Reminder,
        "toilet" to Toilet,
        "trivia" to Trivia,
        "version" to Version,
        "warframe" to Warframe,

        // aliases
        "bugreport" to Issue
    )

    /**
     * Command delegator for all messages.
     *
     * @param event Event triggered by the message.
     */
    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        GlobalScope.launch(Dispatchers.Default) {
            logger.debug("Coroutine launched")
            logger.debug(
                "Handling message \"${event.message.content}\" " +
                        "from ${event.author.discordTag()} " +
                        "in ${event.channel.channelName()} "
            )

            if (!isInvocationValid(event) && !event.isOwnerLocationValid()) {
                logger.debug("Message ignored: Not invoking bot")
                return@launch
            }

            if (TriviaService.checkUserTriviaStatus(event)) {
                logger.debug("Message ignored: User in Trivia session")
                return@launch
            }

            val cmd = getCommand(popLeadingMention(event.message.content)).toLowerCase().let {
                when {
                    it.isBlank() -> it
                    it.last() == '!' && Core.monikaVersionBranch != "development" -> {
                        logger.debug("Message ignored: target=devel")
                        return@launch
                    }
                    it.last() == '!' -> {
                        it.dropLast(1)
                    }
                    Core.monikaVersionBranch == "development" -> {
                        logger.debug("Message ignored: target=stable")
                        return@launch
                    }
                    else -> it
                }
            }

            if (cmd.isBlank()) {
                logger.debug("Message has no command: Getting random response")
                noCommandResponse(event.channel)

                return@launch
            }

            val retval = run {
                val cmdMatches = commands.filter { it.key.startsWith(cmd) }

                ExceptionDisplayer.catchAllEx(event.channel) {
                    when (cmdMatches.size) {
                        0 -> {
                            logger.infoFun(Core.getMethodName()) { "Command not found in primary set. Trying to match emoticons..." }
                            Emoticon.handler(event)
                        }
                        1 -> {
                            if (cmd != cmdMatches.entries.first().key) {
                                MessageHelper.buildMessage(event.channel) {
                                    content {
                                        withContent(":information_source: Assuming you meant ${cmdMatches.entries.first().key}...")
                                    }
                                }
                            }
                            cmdMatches.entries.first().value.delegateCommand(event)
                        }
                        else -> {
                            if (cmdMatches.entries.all { it.value == cmdMatches.entries.first().value }) {
                                MessageHelper.buildMessage(event.channel) {
                                    content {
                                        withContent(":information_source: Assuming you meant ${cmdMatches.entries.first().key}...")
                                    }
                                }
                                cmdMatches.entries.first().value.delegateCommand(event)
                            } else {
                                HandleState.MULTIPLE_MATCHES
                            }
                        }
                    }
                }.also {
                    if (it == null) {
                        logger.debug("Caught unhandled exception! Bailing...")
                        return@launch
                    }
                }
            }

            when (retval) {
                HandleState.NOT_FOUND -> {
                    MessageHelper.buildMessage(event.channel) {
                        content {
                            withContent("I don't know how to do that! >.<")
                        }
                    }
                }
                HandleState.PERMISSION_DENIED -> {
                    MessageHelper.buildMessage(event.channel) {
                        content {
                            withContent("You're not allow to do this! x(")
                        }
                    }
                }
                HandleState.MULTIPLE_MATCHES -> {
                    MessageHelper.buildMessage(event.channel) {
                        content {
                            withContent("Your message matches multiple commands!")

                            appendContent("\n\nYour provided command matches:\n")
                            appendContent(commands.filter {
                                it.key.startsWith(cmd)
                            }.entries.distinctBy {
                                it.value
                            }.joinToString("\n") {
                                "- ${it.key}"
                            })
                        }
                    }
                }
                else -> {
                }
            }
        }.invokeOnCompletion { logger.debug("Coroutine completed") }
    }

    /**
     * Sends a message to [channel] with a null response.
     */
    private fun noCommandResponse(channel: IChannel) {
        val response = nullResponses[java.util.Random().nextInt(nullResponses.size)]

        buildMessage(channel) {
            content {
                withContent(response)
            }
        }
    }

    /**
     * Returns true if the invocation is valid, i.e.:
     *  - In a private channel, or
     *  - Message starts with a mention of the bot.
     */
    private fun isInvocationValid(event: MessageReceivedEvent) =
        event.channel.isPrivate || event.message.isMentionMe()

    /**
     * Reloads responses when bot is invoked but no command is given.
     */
    fun loadNullResponses(): List<String> {
        nullResponses =
                File(Thread.currentThread().contextClassLoader.getResource(NULL_RESPONSE_PATH).toURI()).readLines()
        return nullResponses
    }

    /**
     * Returns the command from a string.
     */
    private fun getCommand(message: String): String = message.split(' ')[0]

    enum class HandleState {
        HANDLED,
        UNHANDLED,
        PERMISSION_DENIED,
        MULTIPLE_MATCHES,
        NOT_FOUND
    }
}
