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
import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.Core.isFromOwner
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.StopService.cleanup
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.isOwnerLocationValid
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Stop : IBase, ILogger {
    override fun cmdName(): String = "stop"

    override fun handlerSu(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        if (!event.isFromOwner()) {
            return CommandInterpreter.HandleState.PERMISSION_DENIED
        } else if (!event.isOwnerLocationValid()) {
            return CommandInterpreter.HandleState.UNHANDLED
        }

        val args = getArgumentList(event.message.content)
        if (args.any { it.matches(Regex("-{0,2}dev(elopment)?")) } && Core.monikaVersionBranch != "development") {
            return CommandInterpreter.HandleState.HANDLED
        }
        if (args.any { it.matches(Regex("-{0,2}stable")) } && Core.monikaVersionBranch != "stable") {
            return CommandInterpreter.HandleState.HANDLED
        }

        val isForced = args.any { it.matches(Regex("-{0,2}force")) }

        cleanup(isForced)

        return CommandInterpreter.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        if (isSu) {
            buildHelpText(cmdInvocation(), event) {
                description { "Stops the execution of the bot." }
                
                usage("[--force] [stable|development]") {
                    flag("force") { "If appended, forcefully shuts down the server without any buffer time." }
                    option("stable") { "Specifies to stop the stable version of the bot. This is the default option." }
                    option("development") { "Specifies to stop the development version of the bot." }
                }
            }
        }
    }
}
