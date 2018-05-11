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

package com.derppening.monikabot.impl

import com.derppening.monikabot.core.Core.isFromSuperuser
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.LocationUtils.getChannelByName
import com.derppening.monikabot.util.LocationUtils.getGuildByName
import com.derppening.monikabot.util.LocationUtils.getUserByTag
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object EchoService : ILogger {
    fun toPrivateChannel(args: List<String>): Result {
        val username = args[1].dropLastWhile { it != '#' }.dropLastWhile { it == '#' }
        val discriminator = args[1].dropWhile { it != '#' }.dropWhile { it == '#' }

        if (username.isBlank() || discriminator.isBlank()) {
            return Result.Failure("Please specify a destination!")
        } else if (discriminator.toIntOrNull() == null) {
            return Result.Failure("The Discord Tag is formatted incorrectly!")
        }

        try {
            val channel = getUserByTag(username, discriminator.toInt())?.orCreatePMChannel
                    ?: error("Cannot find user!")

            val message = args.drop(2).joinToString(" ")
            buildMessage(channel) {
                content {
                    withContent(message)
                }
            }
        } catch (de: DiscordException) {
            de.printStackTrace()
            return Result.Failure("I can't deliver the message! Reason: ${de.errorMessage}")
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.Failure("I can't deliver the message! Reason: ${e.message}")
        }

        return Result.Success()
    }

    fun toGuildChannel(args: List<String>, event: MessageReceivedEvent): Result {
        val guildStr = args[1].dropLastWhile { it != '/' }.dropLastWhile { it == '/' }.let {
            if (it.isBlank() && !event.channel.isPrivate) {
                event.guild.name
            } else {
                it
            }
        }
        val channelStr = args[1].dropWhile { it != '/' }.dropWhile { it == '/' || it == '#' }

        if (guildStr.isBlank() || channelStr.isBlank()) {
            return Result.Failure("Please specify a destination!")
        }

        try {
            val guild = getGuildByName(guildStr) ?: error("Cannot find guild $guildStr")
            val channel = getChannelByName(channelStr, guild) ?: error("Cannot find channel $channelStr")

            if (!event.isFromSuperuser() && !guild.users.contains(event.author)) {
                error("You can only send messages to guilds which you are in!")
            }

            val message = args.drop(2).joinToString(" ")
            buildMessage(channel) {
                content {
                    withContent(message)
                }
            }
        } catch (de: DiscordException) {
            de.printStackTrace()
            return Result.Failure("I can't deliver the message! Reason: ${de.errorMessage}")
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.Failure("I can't deliver the message! Reason: ${e.message}")
        }

        return Result.Success()
    }

    sealed class Result {
        class Success : Result()
        class Failure(val message: String) : Result()
    }
}