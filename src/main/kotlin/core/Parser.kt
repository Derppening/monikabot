/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package core

import cmds.*
import core.BuilderHelper.buildMessage
import core.Core.popLeadingMention
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.io.File

object Parser : IChannelLogger {
    enum class HandleState {
        HANDLED,
        UNHANDLED,
        PERMISSION_DENIED,
        NOT_FOUND
    }

    /**
     * Command delegator for all messages.
     *
     * @param event Event triggered by the message.
     */
    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        if (cmds.experimental.Trivia.checkUserTriviaStatus(event)) { return }

        if (!event.channel.isPrivate &&
                !event.message.content.startsWith(Client.ourUser.mention(false))) {
            if (!Core.isOwnerLocationValid(event)) {
                return
            }
        }

        val cmd = getCommand(popLeadingMention(event.message.content)).toLowerCase()

        if (cmd.isBlank()) {
            buildMessage(event.channel) {
                withContent(getRandomNullResponse())
            }
            return
        }

        val runExperimental = event.message.content.split(' ').any { it == "--experimental" }
        val retval: HandleState
        if (runExperimental && Config.enableExperimentalFeatures) {
            retval = parseExperimental(event, cmd)
        } else {
            if (runExperimental) {
                buildMessage(event.channel) {
                    if (Core.isEventFromSuperuser(event)) {
                        withContent("It seems like you're trying to invoke an experimental command without it being on...")
                    } else {
                        withContent("Experimental features are turned off! If you want to test it, ask the owner to turn it on!")
                    }
                }
            }

            retval = when (cmd) {
                "changelog" -> Changelog.delegateCommand(event)
                "clear" -> Clear.delegateCommand(event)
                "config" -> Config.delegateCommand(event)
                "debug" -> Debug.delegateCommand(event)
                "echo" -> Echo.delegateCommand(event)
                "help" -> Help.delegateCommand(event)
                "random" -> Random.delegateCommand(event)
                "reload" -> Reload.delegateCommand(event)
                "rng" -> RNG.delegateCommand(event)
                "status" -> Status.delegateCommand(event)
                "stop" -> Stop.delegateCommand(event)
                "version" -> Version.delegateCommand(event)
                "warframe" -> Warframe.delegateCommand(event)
                else -> HandleState.NOT_FOUND
            }
        }

        postCommandHandler(retval, cmd, event)
    }

    /**
     * Reloads responses when bot is invoked but no command is given.
     */
    fun loadNullResponses(): List<String> {
        nullResponses = File(Thread.currentThread().contextClassLoader.getResource("lang/NullResponse.txt").toURI()).readLines()
        return nullResponses
    }

    /**
     * Parses commands with "--experimental" flag given.
     */
    private fun parseExperimental(event: MessageReceivedEvent, cmd: String): HandleState {
        return when (cmd) {
            "trivia" -> cmds.experimental.Trivia.delegateCommand(event)
            else -> HandleState.NOT_FOUND
        }
    }

    /**
     * Handler for after the command is delegated.
     *
     * @param state Whether the command is successfully handled.
     * @param cmd The original invoked command.
     * @param event The event triggered by the message.
     */
    private fun postCommandHandler(state: HandleState, cmd: String, event: MessageReceivedEvent) {
        when (state) {
            HandleState.HANDLED -> {}
            HandleState.UNHANDLED -> {
                log(IChannelLogger.LogLevel.ERROR, "Command \"$cmd\" not handled") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
            HandleState.NOT_FOUND -> {
                try {
                    buildMessage(event.channel) {
                        withContent("I don't know how to do that! >.<")
                    }
                } catch (e: DiscordException) {}
                log(IChannelLogger.LogLevel.ERROR, "Command \"$cmd\" not found") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
            HandleState.PERMISSION_DENIED -> {
                try {
                    buildMessage(event.channel) {
                        withContent("You're not allow to do this! x(")
                    }
                } catch (e: DiscordException) {}
                log(IChannelLogger.LogLevel.ERROR, "\"$cmd\" was not invoked by superuser") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
        }
    }

    /**
     * Returns the command from a string.
     */
    private fun getCommand(message: String): String = message.split(' ')[0]

    /**
     * Returns a random message from nullResponses.
     */
    private fun getRandomNullResponse(): String = nullResponses[java.util.Random().nextInt(nullResponses.size)]

    private var nullResponses = loadNullResponses()
}