/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package cmds

import core.BuilderHelper.buildEmbed
import core.Client
import core.Core
import core.IChannelLogger
import core.Parser
import insertSeparator
import removeQuotes
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.DiscordException

object Status : IBase {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        if (!Core.isEventFromOwner(event)) {
            return Parser.HandleState.PERMISSION_DENIED
        } else if (!Core.isOwnerLocationValid(event)) {
            return Parser.HandleState.UNHANDLED
        }

        val args = getArgumentList(event.message.content).toMutableList()

        if (args.size == 0) {
            Client.resetStatus()

            return Parser.HandleState.HANDLED
        }

        val status = when (args[0]) {
            "--reset" -> {
                Client.resetStatus()
                return Parser.HandleState.HANDLED
            }
            "--idle" -> {
                args.removeAt(0)
                StatusType.IDLE
            }
            "--dnd", "--busy" -> {
                args.removeAt(0)
                StatusType.DND
            }
            "--offline", "--invisible" -> {
                args.removeAt(0)
                StatusType.INVISIBLE
            }
            "--online" -> {
                args.removeAt(0)
                StatusType.ONLINE
            }
            else -> StatusType.ONLINE
        }

        val activity = when (args[0]) {
            "--play", "--playing" -> {
                args.removeAt(0)
                ActivityType.PLAYING
            }
            "--stream", "--streaming" -> {
                args.removeAt(0)
                ActivityType.STREAMING
            }
            "--listen", "--listening" -> {
                args.removeAt(0)
                ActivityType.LISTENING
            }
            "--watch", "--watching" -> {
                args.removeAt(0)
                ActivityType.WATCHING
            }
            else -> ActivityType.PLAYING
        }

        val arg = args.joinToString(" ").removeQuotes()
        val streamUrl = arg.substringAfter("--").trim().dropWhile { it == '<' }.dropLastWhile { it == '>' }
        val message = arg.let {
            if (activity == ActivityType.STREAMING) {
                it.substringBefore("--").trim()
            } else {
                it
            }
        }

        try {
            if (activity == ActivityType.STREAMING) {
                Client.changeStreamingPresence(status, message, streamUrl)
            } else {
                Client.changePresence(status, activity, message)
            }
            log(IChannelLogger.LogLevel.INFO, "Successfully updated")
        } catch (e: Exception) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot set status") {
                author { event.author }
                channel { event.channel }
                info { e.message ?: "Unknown Exception" }
            }
            e.printStackTrace()
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `status`")
                withDesc("Sets the status and playing text of the bot.")
                insertSeparator()
                appendField("Usage", "```status [STATUS] [ACTIVITY] [TEXT] -- [URL]```", false)
                appendField("`[STATUS]`", "New status for the bot. Can be one of the following:" +
                        "\n\t`--online`\n\t--idle\n\t--dnd\n\t--offline", false)
                appendField("`[ACTIVITY]`", "New activity for the bot. Can be one of the following:" +
                        "\n\t`--play`\n\t`--stream`\n\t`--listen`\n\t`--watch`", false)
                appendField("`[TEXT]`", "New \"Playing\" message of the bot.", false)
                appendField("`[URL]`", "If `ACTIVITY` is set to streaming, the link of the Twitch stream.", false)
                insertSeparator()
                appendField("Usage", "```status [--reset]```", false)
                appendField("`--reset`", "Resets the status to the default.", false)
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Unable to display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }
}
