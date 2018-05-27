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
import com.derppening.monikabot.impl.ReloadService.commitReload
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Reload : IBase, ILogger {
    override fun cmdName(): String = "reload"

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        commitReload()

        log(ILogger.LogLevel.INFO, "Properties have been reloaded.") {
            author { event.author }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("reload", event) {
            description { "Reloads essential bot properties from their respective files." }

            usage("reload")
        }
    }
}