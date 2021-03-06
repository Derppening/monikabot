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
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.EmoticonService
import com.derppening.monikabot.impl.EmoticonService.findEmoticon
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import com.derppening.monikabot.util.popLeadingMention
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Emoticon : IBase, ILogger {
    override fun cmdName(): String = ""

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val str = popLeadingMention(event.message.content, event.guild).dropLastWhile { it == '!' }

        return findEmoticon(str).let {
            when (it) {
                is EmoticonService.Result.Success -> {
                    buildMessage(event.channel) {
                        content {
                            withContent(it.emote)
                        }
                    }
                    CommandInterpreter.HandleState.HANDLED
                }
                is EmoticonService.Result.Failure -> {
                    if (it.message.isNotBlank()) {
                        buildMessage(event.author.orCreatePMChannel) {
                            content {
                                withContent(it.message)
                            }
                        }
                    }
                    it.state
                }
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        throw IllegalStateException("Emoticon should not be able to invoke help()")
    }
}