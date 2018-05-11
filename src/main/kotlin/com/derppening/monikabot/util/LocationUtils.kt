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
import com.derppening.monikabot.core.ILogger
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IPrivateChannel
import sx.blah.discord.handle.obj.IUser

object LocationUtils : ILogger {

    /**
     * @return Discord tag.
     */
    fun IUser.getDiscordTag(): String = "$name#$discriminator"

    /**
     * @return Channel name in "Server/Channel" format.
     */
    fun IChannel.getChannelName(): String = "${if (this is IPrivateChannel) "[Private]" else guild.name}/$name"

    /**
     * @param username User name portion of the Discord Tag.
     * @param discriminator Discriminator portion of the Discord Tag.
     *
     * @return IUser matching "[username]#[discriminator]"
     */
    fun getUserByTag(username: String, discriminator: Int): IUser? {
        return Client.getUsersByName(username).find { it.discriminator == discriminator.toString() }
    }

    /**
     * @param name Name of the guild.
     *
     * @return IGuild matching the guild name.
     */
    fun getGuildByName(name: String): IGuild? {
        return Client.guilds.find { it.name == name }
    }

    /**
     * @param name Name of channel.
     * @param guild Guild.
     *
     * @return IChannel matching the channel name within the guild.
     */
    fun getChannelByName(name: String, guild: IGuild): IChannel? {
        return guild.channels.find { it.name == name }
    }
}