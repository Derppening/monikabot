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

package com.derppening.monikabot.cmds

import com.derppening.monikabot.core.*
import com.derppening.monikabot.core.BuilderHelper.buildEmbed
import com.derppening.monikabot.core.BuilderHelper.buildMessage
import com.derppening.monikabot.core.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import kotlin.concurrent.thread

object Config : IBase, ILogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        if (args.isEmpty()) {
            help(event, true)
            return Parser.HandleState.HANDLED
        }

        when (args[0]) {
            "experimental" -> {
                experimentalHandler(args, event)
            }
            "owner_echo_for_su" -> {
                ownerModeEchoHandler(args, event)
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        BuilderHelper.buildEmbed(event.channel) {
            withTitle("Help Text for `config`")
            withDesc("Core configurations for MonikaBot.")
            insertSeparator()
            appendField("Usage", "```config [configuration] [options...]```", false)
            appendField("Configuration: `experimental`", "Whether to enable experimental features", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    /**
     * Handler for "config experimental" commands.
     *
     * @param args List of arguments.
     * @param event Event of the original message.
     */
    private fun experimentalHandler(args: List<String>, event: MessageReceivedEvent) {
        if (args.size == 1) {
            buildMessage(event.channel) {
                withContent("Experimental Features: ${if (enableExperimentalFeatures) "Enabled" else "Disabled"}.")
            }

            return
        } else if (args.size != 2 || args[1].matches(Regex("-{0,2}help"))) {
            buildEmbed(event.channel) {
                withTitle("Help Text for config-experimental`")
                withDesc("Whether to enable experimental features.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```config experimental [enable|disable]```", false)
                appendField("`[enable|disable]`", "Enables/Disables experimental features.", false)
            }
            return
        }

        enableExperimentalFeatures = args[1].toBoolean() || args[1] == "enable"
        buildMessage(event.channel) {
            withContent("Experimental Features are now ${if (enableExperimentalFeatures) "enabled" else "disabled"}.")
        }

        thread {
            PersistentMessage.modify("Config", "Experimental Features", enableExperimentalFeatures.toString(), true)
        }
    }

    /**
     * Handler for "config owner_echo_for_su" commands.
     *
     * @param args List of arguments.
     * @param event Event of the original message.
     */
    private fun ownerModeEchoHandler(args: List<String>, event: MessageReceivedEvent) {
        if (args.size == 1) {
            buildMessage(event.channel) {
                withContent("Owner Mode Echo for Superusers: ${if (ownerModeEchoForSu) "Allow" else "Deny"}.")
            }

            return
        } else if (args.size != 2 || args[1].matches(Regex("-{0,2}help"))) {
            buildEmbed(event.channel) {
                withTitle("Help Text for config-owner_echo_for_su`")
                withDesc("Whether to allow superusers access to owner mode `echo`.")
                appendField("\u200B", "\u200B", false)
                appendField("Usage", "```config owner_echo_for_su [allow|deny]```", false)
                appendField("`[allow|deny]`", "Allows or denies owner mode echo for superusers.", false)
            }
            return
        }

        enableExperimentalFeatures = args[1] == "allow"
        buildMessage(event.channel) {
            withContent("Experimental Features are now ${if (ownerModeEchoForSu) "allowed" else "denied"}.")
        }

        thread {
            PersistentMessage.modify("Config", "Owner Mode Echo for Superusers", ownerModeEchoForSu.toString(), true)
        }
    }

    /**
     * Whether to enable experimental features.
     */
    var enableExperimentalFeatures = Core.monikaVersionBranch == "development"
        private set
    /**
     * Whether to allow superusers to access owner mode echo.
     */
    var ownerModeEchoForSu = true
        private set
}