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

package com.derppening.monikabot.core

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.obj.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object Core {
    /**
     * Whether action is performed in a superuser channel (currently only in PM or MonikaBot/debug)
     */
    fun MessageEvent.isOwnerLocationValid() = channel == serverDebugChannel || channel == ownerPrivateChannel

    /**
     * @return Whether event is from the bot owner.
     */
    fun MessageEvent.isFromOwner() = author.longID == ownerId

    /**
     * @return Whether event is from a superuser.
     */
    fun MessageEvent.isFromSuperuser() = suIds.any { it == author.longID }

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

    /**
     * Performs a full reload of the bot.
     */
    fun reload() {
        loadVersion()
        loadSuIds()

        thread {
            PersistentMessage.modify("Misc", "Version", monikaVersion, true)
        }
    }

    /**
     * Loads the bot's version and returns itself.
     */
    private fun loadVersion(): String {
        monikaVersion = "${loadSemVersion()}+${monikaVersionBranch}"
        return monikaVersion
    }

    /**
     * Loads the bot's SemVer portion of version and returns itself.
     */
    private fun loadSemVersion(): String {
        monikaSemVersion = getProperties(VERSION_PROP).getProperty("version")!!
        return monikaSemVersion
    }

    /**
     * Loads superuser IDs and returns itself.
     */
    private fun loadSuIds(): Set<Long> {
        suIds = getProperties(SOURCE_PROP)
                .getProperty("suId")
                .split(',')
                .map { it.toLong() }
                .union(listOf(ownerId))
        return suIds
    }

    /**
     * Gets the method name which invoked this method.
     */
    fun getMethodName(vararg args: String): String {
        return Thread.currentThread().stackTrace[2].methodName + "(${args.joinToString(", ")})"
    }

    /**
     * Remove quotes from a word.
     */
    fun String.removeQuotes(): String = dropWhile { it == '\"' }.dropLastWhile { it == '\"' }

    /**
     * Loads a property object based on a file. Application will terminate if file cannot be found.
     *
     * @param filename Filename of properties file to load.
     *
     * @return Properties object of loaded file.
     */
    private fun getProperties(filename: String): Properties {
        try {
            return Properties().apply {
                val relpath = "properties/$filename"
                load(FileInputStream(File(Thread.currentThread().contextClassLoader.getResource(relpath).toURI())))
            }
        } catch (ioException: FileNotFoundException) {
            println("Cannot find properties file")
            ioException.printStackTrace()

            exitProcess(0)
        }
    }

    /**
     * Pops the first word in a string.
     */
    private fun String.popFirstWord(): String = dropWhile { it != ' ' }.dropWhile { it == ' ' }

    /**
     * Filename of source.properties.
     */
    private const val SOURCE_PROP = "source.properties"
    /**
     * Filename of version.properties.
     */
    private const val VERSION_PROP = "version.properties"

    /**
     * PM Channel of bot admin.
     */
    val ownerPrivateChannel: IPrivateChannel by lazy { Client.fetchUser(ownerId).orCreatePMChannel }
    /**
     * Debug channel.
     */
    val serverDebugChannel: IChannel? by lazy { Client.getChannelByID(serverDebugChannelId) }
    /**
     * Bot private key.
     */
    val privateKey = getProperties(SOURCE_PROP).getProperty("privateKey")!!

    /**
     * SemVer version of the bot.
     */
    var monikaSemVersion = loadSemVersion()
        private set
    /**
     * The git branch of this bot.
     */
    val monikaVersionBranch = getProperties(VERSION_PROP).getProperty("gitbranch")!!
    /**
     * Version of the bot.
     */
    var monikaVersion = loadVersion()
        private set

    /**
     * ID of bot admin.
     */
    private val ownerId = getProperties(SOURCE_PROP).getProperty("adminId").toLong()
    /**
     * IDs for bot superusers.
     */
    private var suIds = loadSuIds()
    /**
     * ID of Debug channel.
     */
    private val serverDebugChannelId = getProperties(SOURCE_PROP).getProperty("debugChannelId").toLong()
}
