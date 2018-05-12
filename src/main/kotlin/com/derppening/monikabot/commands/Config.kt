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
import com.derppening.monikabot.impl.ConfigService
import com.derppening.monikabot.impl.ConfigService.configureExperimentalFlag
import com.derppening.monikabot.impl.ConfigService.configureOwnerEchoFlag
import com.derppening.monikabot.impl.ConfigService.enableExperimentalFeatures
import com.derppening.monikabot.impl.ConfigService.ownerModeEchoForSu
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import com.derppening.monikabot.util.helpers.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

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

    /**
     * Handler for "config experimental" commands.
     *
     * @param args List of arguments.
     * @param event Event of the original message.
     */
    private fun experimentalHandler(args: List<String>, event: MessageReceivedEvent) {
        when (configureExperimentalFlag(args)) {
            ConfigService.Result.GET -> {
                buildMessage(event.channel) {
                    content {
                        withContent("Experimental Features: ${if (enableExperimentalFeatures) "Enabled" else "Disabled"}.")
                    }
                }
            }
            ConfigService.Result.SET -> {
                buildMessage(event.channel) {
                    content {
                        withContent("Experimental Features are now ${if (enableExperimentalFeatures) "enabled" else "disabled"}.")
                    }
                }
            }
            ConfigService.Result.HELP -> {
                buildEmbed(event.channel) {
                    fields {
                        withTitle("Help Text for config-experimental`")
                        withDesc("Whether to enable experimental features.")
                        insertSeparator()
                        appendField("Usage", "```config experimental [enable|disable]```", false)
                        appendField("`[enable|disable]`", "Enables/Disables experimental features.", false)
                    }
                }
            }
        }
    }

    /**
     * Handler for "config owner_echo_for_su" commands.
     *
     * @param args List of arguments.
     * @param event Event of the original message.
     */
    private fun ownerModeEchoHandler(args: List<String>, event: MessageReceivedEvent) {
        when (configureOwnerEchoFlag(args)) {
            ConfigService.Result.GET -> {
                buildMessage(event.channel) {
                    content {
                        withContent("Owner Mode Echo for Superusers: ${if (ownerModeEchoForSu) "Allow" else "Deny"}.")
                    }
                }
            }
            ConfigService.Result.SET -> {
                buildMessage(event.channel) {
                    content {
                        withContent("Experimental Features are now ${if (ownerModeEchoForSu) "allowed" else "denied"}.")
                    }
                }
            }
            ConfigService.Result.HELP -> {
                buildEmbed(event.channel) {
                    fields {
                        withTitle("Help Text for config-owner_echo_for_su`")
                        withDesc("Whether to allow superusers access to owner mode `echo`.")
                        insertSeparator()
                        appendField("Usage", "```config owner_echo_for_su [allow|deny]```", false)
                        appendField("`[allow|deny]`", "Allows or denies owner mode echo for superusers.", false)
                    }
                }
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            fields {
                withTitle("Help Text for `config`")
                withDesc("Core configurations for MonikaBot.")
                insertSeparator()
                appendField("Usage", "```config [configuration] [options...]```", false)
                appendField("Configuration: `experimental`", "Whether to enable experimental features", false)
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