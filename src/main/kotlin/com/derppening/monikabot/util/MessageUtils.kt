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

package com.derppening.monikabot.util

import com.derppening.monikabot.core.Client
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage

/**
* Returns true if given message mentions the bot as the first token.
*/
fun IMessage.isMentionMe(): Boolean =
        content.startsWith(Client.ourUser.mention()) || content.startsWith(Client.ourUser.mention(false))

/**
 * Removes the leading MonikaBot mention from a message.
 *
 * @param message Original message.
 * @param guild Guild where the message is sent, if any.
 *
 * @return Message without a leading mention.
 */
fun popLeadingMention(message: String, guild: IGuild? = null): String {
    return when {
        message.startsWith(Client.ourUser.mention()) ||
                message.startsWith(Client.ourUser.mention(false)) ||
                message.startsWith("@${Client.ourUser.name}") -> {
            message.popFirstWord()
        }
        guild != null && message.startsWith("@${Client.ourUser.getNicknameForGuild(guild)}") -> {
            message.popFirstWord()
        }
        else -> {
            message
        }
    }
}

/**
 * Remove quotes from a word.
 */
fun String.removeQuotes(): String = dropWhile { it == '\"' }.dropLastWhile { it == '\"' }

/**
 * Pops the first word in a string.
 */
private fun String.popFirstWord(): String = dropWhile { it != ' ' }.dropWhile { it == ' ' }
