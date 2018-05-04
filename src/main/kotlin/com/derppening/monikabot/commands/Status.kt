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

import com.derppening.monikabot.core.Core.isFromOwner
import com.derppening.monikabot.core.Core.isOwnerLocationValid
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.StatusService.setNewStatus
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Status : IBase {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        if (!event.isFromOwner()) {
            return Parser.HandleState.PERMISSION_DENIED
        } else if (!event.isOwnerLocationValid()) {
            return Parser.HandleState.UNHANDLED
        }

        val args = getArgumentList(event.message.content).toMutableList()

        setNewStatus(args)

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `status`")
            withDesc("Sets the status and playing text of the bot.")
            insertSeparator()
            appendField("Usage", "```status [STATUS] [ACTIVITY] [TEXT] -- [URL]```", false)
            appendField("`[STATUS]`", "New status for the bot. Can be one of the following:" +
                    "\n\t`--online`\n\t`--idle`\n\t`--dnd`\n\t`--offline`", false)
            appendField("`[ACTIVITY]`", "New activity for the bot. Can be one of the following:" +
                    "\n\t`--play`\n\t`--stream`\n\t`--listen`\n\t`--watch`", false)
            appendField("`[TEXT]`", "New \"Playing\" message of the bot.", false)
            appendField("`[URL]`", "If `ACTIVITY` is set to streaming, the link of the Twitch stream.", false)
            insertSeparator()
            appendField("Usage", "```status [--reset]```", false)
            appendField("`--reset`", "Resets the status to the default.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Unable to display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }
}
