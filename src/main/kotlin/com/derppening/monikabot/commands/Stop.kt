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

import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.Core.isFromOwner
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.StopService.cleanup
import com.derppening.monikabot.util.EventUtils.isOwnerLocationValid
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Stop : IBase, ILogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        if (!event.isFromOwner()) {
            return Parser.HandleState.PERMISSION_DENIED
        } else if (!event.isOwnerLocationValid()) {
            return Parser.HandleState.UNHANDLED
        }

        val args = getArgumentList(event.message.content)
        if (args.any { it.matches(Regex("-{0,2}dev(elopment)?")) } && Core.monikaVersionBranch != "development") {
            return Parser.HandleState.HANDLED
        }
        if (args.any { it.matches(Regex("-{0,2}stable")) } && Core.monikaVersionBranch != "stable") {
            return Parser.HandleState.HANDLED
        }

        val isForced = args.any { it.matches(Regex("-{0,2}force")) }

        cleanup(isForced)

        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        if (isSu) {
            buildEmbed(event.channel) {
                fields {
                    withTitle("Help Text for `stop`")
                    withDesc("Stops the execution of the bot.")
                    insertSeparator()
                    appendField("Usage", "```stop [--force] [stable|development]```", false)
                    appendField("`--force", "If appended, forcefully shuts down the server without any buffer time.", false)
                    appendField("`[stable|development]`", "Optional: Which specific instance(s) to stop.", false)
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
}
