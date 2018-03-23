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

package cmds

import core.BuilderHelper.buildEmbed
import core.Core
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

interface IBase : IChannelLogger {
    /**
     * Delegates [event] to the appropriate function.
     *
     * @return Whether the action is handled.
     */
    fun delegateCommand(event: MessageReceivedEvent): Parser.HandleState {
        if (hasHelpFlag(event.message.content)) {
            help(event, Core.isEventFromSuperuser(event))
            return Parser.HandleState.HANDLED
        }

        if (Core.isEventFromSuperuser(event)) {
            val suHandleStatus = handlerSu(event)
            if (suHandleStatus != Parser.HandleState.UNHANDLED) {
                return suHandleStatus
            }
        }

        return handler(event)
    }

    /**
     * Shows help text for the command.
     *
     * @param event: The event leading to the invocation of the this function.
     * @param isSu: Whether user invoking this function is a superuser.
     */
    fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text")
            withDesc("No help text is available for this command.")

            onDiscordError { e ->
                log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
                e.printStackTrace()
            }
        }
    }

    /**
     * Handles [event] for all users.
     *
     * @return Whether the action is handled.
     */
    fun handler(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.PERMISSION_DENIED
    }

    /**
     * Handles [event] for superusers.
     *
     * @return Whether the action is handled.
     */
    fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        return Parser.HandleState.UNHANDLED
    }

    /**
     * @return List of arguments.
     */
    fun getArgumentList(str: String): List<String> {
        val cmdStr = Core.popLeadingMention(str)
        val tokens = cmdStr.split(" ").drop(1).joinToString(" ")
        val list = mutableListOf<String>()

        var parseQuote = false
        var s = ""
        tokens.forEach {
            when {
                it == '\"' -> {
                    if (parseQuote) {
                        if (s.isNotBlank()) list.add(s)
                        s = ""
                    }
                    parseQuote = !parseQuote
                }
                it == ' ' && !parseQuote -> {
                    if (s.isNotBlank()) list.add(s)
                    s = ""
                }
                else -> s += it
            }
        }
        if (s.isNotBlank()) list.add(s)

        return list.toList()
    }

    /**
     * Check whether argument list contains a "help" flag.
     */
    private fun hasHelpFlag(arg0: String): Boolean {
        return getArgumentList(arg0)
                .dropWhile { it == "--experimental" }
                .also { if (it.isEmpty()) return false }[0]
                .matches(Regex("-{0,2}help"))
    }
}