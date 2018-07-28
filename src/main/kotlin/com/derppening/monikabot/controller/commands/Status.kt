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
import com.derppening.monikabot.core.Core.isFromOwner
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.StatusService.setNewStatus
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.isOwnerLocationValid
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Status : IBase, ILogger {
    override fun cmdName(): String = "status"

    override fun handlerSu(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        if (!event.isFromOwner()) {
            return CommandInterpreter.HandleState.PERMISSION_DENIED
        } else if (!event.isOwnerLocationValid()) {
            return CommandInterpreter.HandleState.UNHANDLED
        }

        val args = getArgumentList(event.message.content).toMutableList()

        setNewStatus(args)

        return CommandInterpreter.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("status", event) {
            description { "Sets the status and playing text of the bot." }

            usage("status [STATUS] [ACTIVITY] [TEXT] -- [URL]") {
                def("[STATUS]") {
                    "New status for the bot. Can be one of the following:" +
                            "\n\t`--online`" +
                            "\n\t`--idle`" +
                            "\n\t`--dnd`" +
                            "\n\t`--offline`"
                }
                def("[ACTIVITY]") {
                    "New activity for the bot. Can be one of the following:" +
                            "\n\t`--play`" +
                            "\n\t`--stream`" +
                            "\n\t`--listen`" +
                            "\n\t`--watch`"
                }
                def("[TEXT]") { "New \"Playing\" message of the bot." }
                def("[URL]") { "If `ACTIVITY` is set to streaming, the link of the Twitch stream." }
            }
            usage("status [--reset]") {
                def("--reset") { "Resets the status to the default." }
            }
        }
    }
}
