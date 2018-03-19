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

package core

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IPrivateChannel
import sx.blah.discord.handle.obj.IUser
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
    fun isOwnerLocationValid(event: MessageEvent) =
            event.channel == serverDebugChannel || event.channel == ownerPrivateChannel
    /**
     * @return Whether event is from the bot owner.
     */
    fun isEventFromOwner(event: MessageEvent) =
            event.author.longID == ownerId
    /**
     * @return Whether event is from a superuser.
     */
    fun isEventFromSuperuser(event: MessageEvent) = suIds.any { it == event.author.longID }

    /**
     * Removes the leading MonikaBot mention from a message.
     *
     * @return Message without a leading mention.
     */
    fun popLeadingMention(message: String): String {
        return if (message.startsWith(Client.ourUser.mention(false))) {
            message.popFirstWord()
        } else {
            message
        }
    }

    /**
     * @return Discord tag.
     */
    fun getDiscordTag(user: IUser): String = "${user.name}#${user.discriminator}"

//    fun getUserId(username: String, discriminator: Int): IUser {
//        return Persistence.client.getUsersByName(username).find { it.discriminator == discriminator.toString() }
//    }

//    fun getGuildByName(name: String): IGuild {
//        return Persistence.client.guilds.find { it.name == name }
//    }

//    fun getChannelByName(name: String, guild: IGuild): IChannel {
//        return guild.channels.find { it.name == name }
//    }

    /**
     * @return Channel name in "Server/Channel" format.
     */
    fun getChannelName(channel: IChannel): String {
        val guild = if (channel is IPrivateChannel) "[Private]" else channel.guild.name
        return "$guild/${channel.name}"
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
        monikaVersion = "${loadSemVersion()}+$monikaVersionBranch"
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
    fun getMethodName(): String {
        return Thread.currentThread().stackTrace[2].methodName + "(?)"
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
