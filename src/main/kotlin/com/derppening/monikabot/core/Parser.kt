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

package com.derppening.monikabot.core

import com.derppening.monikabot.cmds.*
import com.derppening.monikabot.cmds.experimental.Dog
import com.derppening.monikabot.cmds.experimental.Emoticon
import com.derppening.monikabot.core.Core.getChannelName
import com.derppening.monikabot.core.Core.getDiscordTag
import com.derppening.monikabot.core.Core.isFromSuperuser
import com.derppening.monikabot.core.Core.isMentionMe
import com.derppening.monikabot.core.Core.isOwnerLocationValid
import com.derppening.monikabot.core.Core.popLeadingMention
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.io.File
import kotlin.concurrent.thread

object Parser : ILogger {
    enum class HandleState {
        HANDLED,
        UNHANDLED,
        PERMISSION_DENIED,
        MULTIPLE_MATCHES,
        NOT_FOUND
    }

    /**
     * Command delegator for all messages.
     *
     * @param event Event triggered by the message.
     */
    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        thread(name = "Delegator Thread (${event.messageID} from ${event.author.getDiscordTag()})") {
            logger.debug("Thread detached")
            logger.debug("Message \"${event.message.content}\" " +
                    "from ${event.author.getDiscordTag()} " +
                    "in ${event.channel.getChannelName()} " +
                    "has ID ${event.messageID}")

            if (!isInvocationValid(event) && !event.isOwnerLocationValid()) {
                logger.debug("Message ${event.messageID} ignored")
                return@thread
            }

            if (Trivia.checkUserTriviaStatus(event)) {
                logger.debug("Message ${event.messageID} ignored: User in Trivia session")
                return@thread
            }

            val cmd = getCommand(popLeadingMention(event.message.content)).toLowerCase().let {
                when {
                    it.last() == '!' && Core.monikaVersionBranch != "development" -> {
                        logger.debug("Message ${event.messageID} ignored: Development version requested")
                        return@thread
                    }
                    it.last() == '!' -> {
                        it.dropLast(1)
                    }
                    Core.monikaVersionBranch == "development" -> {
                        logger.debug("Message ${event.messageID} ignored: Stable version requested")
                        return@thread
                    }
                    else -> it
                }
            }

            if (cmd.isBlank()) {
                logger.debug("Message ${event.messageID} has no command")
                buildMessage(event.channel) {
                    withContent(getRandomNullResponse())
                }
                return@thread
            }

            val runExperimental = event.message.content.split(' ').any { it == "--experimental" }
            val retval = if (runExperimental && Config.enableExperimentalFeatures) {
                parseExperimental(event, cmd)
            } else {
                if (runExperimental) {
                    buildMessage(event.channel) {
                        if (event.isFromSuperuser()) {
                            withContent("It seems like you're trying to invoke an experimental command without it being on...")
                        } else {
                            withContent("Experimental features are turned off! If you want to test it, ask the owner to turn it on!")
                        }
                    }
                }

                val cmdMatches = commands.filter { it.key.startsWith(cmd) }
                when (cmdMatches.size) {
                    0 -> {
                        logger.info("Command not found in primary set. Trying to match emoticons...")
                        Emoticon.handler(event)
                    }
                    1 -> {
                        if (cmd != cmdMatches.entries.first().key) {
                            buildMessage(event.channel) {
                                withContent(":information_source: Assuming you meant ${cmdMatches.entries.first().key}...")
                            }
                        }
                        cmdMatches.entries.first().value.delegateCommand(event)
                    }
                    else -> {
                        if (cmdMatches.entries.all { it.value == cmdMatches.entries.first().value }) {
                            buildMessage(event.channel) {
                                withContent(":information_source: Assuming you meant ${cmdMatches.entries.first().key}...")
                            }
                            cmdMatches.entries.first().value.delegateCommand(event)
                        } else {
                            HandleState.MULTIPLE_MATCHES
                        }
                    }
                }
            }

            when (retval) {
                HandleState.NOT_FOUND -> {
                    buildMessage(event.channel) {
                        withContent("I don't know how to do that! >.<")
                    }
                }
                HandleState.PERMISSION_DENIED -> {
                    buildMessage(event.channel) {
                        withContent("You're not allow to do this! x(")
                    }
                }
                HandleState.MULTIPLE_MATCHES -> {
                    buildMessage(event.channel) {
                        withContent("Your message matches multiple commands!")
                        appendContent("\n\nYour provided command matches:\n")
                        appendContent(commands.filter { it.key.startsWith(cmd) }.entries.distinctBy { it.value }.joinToString("\n") { "- ${it.key}" })
                    }
                }
                else -> {
                }
            }
            logger.info("Joining thread")
        }
    }

    /**
     * Reloads responses when bot is invoked but no command is given.
     */
    fun loadNullResponses(): List<String> {
        nullResponses = File(Thread.currentThread().contextClassLoader.getResource(NULL_RESPONSE_PATH).toURI()).readLines()
        return nullResponses
    }

    /**
     * Parses commands with "--experimental" flag given.
     */
    private fun parseExperimental(event: MessageReceivedEvent, cmd: String): HandleState {
        return experimentalCommands[cmd]?.delegateCommand(event) ?: HandleState.NOT_FOUND
    }

    /**
     * Returns true if the invocation is valid, i.e.:
     *  - In a private channel, or
     *  - Message starts with a mention of the bot.
     */
    private fun isInvocationValid(event: MessageReceivedEvent) =
            event.channel.isPrivate || event.message.isMentionMe()


    /**
     * Returns the command from a string.
     */
    private fun getCommand(message: String): String = message.split(' ')[0]

    /**
     * Returns a random message from nullResponses.
     */
    private fun getRandomNullResponse(): String = nullResponses[java.util.Random().nextInt(nullResponses.size)]

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
            "ping" to Ping,
            "random" to Random,
            "reload" to Reload,
            "reminder" to Reminder,
            "rng" to RNG,
            "status" to Status,
            "stop" to Stop,
            "timer" to Reminder,
            "trivia" to Trivia,
            "version" to Version,
            "warframe" to Warframe,

            // aliases
            "bugreport" to Issue
    )

    private val experimentalCommands: Map<String, IBase> = mapOf(
    )
}