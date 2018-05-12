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

import com.derppening.monikabot.core.Core
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.EmbedHelper.sendEmbed
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IChannel
import java.awt.Color

object ExceptionDisplayer {
    fun <T> catchAllEx(channel: IChannel, actions: () -> T): T? {
        return try {
            actions()
        } catch (e: Exception) {
            sendEmbed(formatExceptionEmbed(e) to channel)
            e.printStackTrace()
            null
        }
    }

    private fun formatExceptionEmbed(e: Exception): EmbedObject {
        return buildEmbed {
            val message = e.message.let {
                if (it.isNullOrBlank()) { "Unknown Exception" } else { it }
            }
            val srcFun = e.stackTrace.first { it.toString().startsWith("com.derppening.monikabot") }.toString()

            withTitle("An unexpected error has occurred!")
            appendField("Type", e.toString().takeWhile { it != ':' }, false)
            appendField("Error Message", message, false)
            appendField("Source Function", srcFun, false)
            e.stackTrace.first().toString().takeIf { it != srcFun }?.let {
                appendField("Topmost Function in Stack", it, false)
            }

            withColor(Color.RED)
            withFooterText("Please take a screenshot of this error with the command you typed, " +
                    "and send it to ${Core.ownerUser.discordTag()}.")
        }.build()
    }
}