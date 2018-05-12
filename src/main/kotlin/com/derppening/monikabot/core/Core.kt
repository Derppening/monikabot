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
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IPrivateChannel
import sx.blah.discord.handle.obj.IUser
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

object Core : ILogger {
    /**
     * Filename of source.properties.
     */
    private const val SOURCE_PROP = "source.properties"
    /**
     * Filename of version.properties.
     */
    private const val VERSION_PROP = "version.properties"

    /**
     * API Key for CheckWX.
     */
    val checkwxKey: String?
    /**
     * Bot private key.
     */
    val privateKey: String

    /**
     * IUser object of bot admin.
     */
    val ownerUser: IUser by lazy { Client.fetchUser(ownerId) }
    /**
     * PM Channel of bot admin.
     */
    val ownerPrivateChannel: IPrivateChannel by lazy { ownerUser.orCreatePMChannel }
    /**
     * ID of bot admin.
     */
    private val ownerId: Long

    /**
     * Debug channel.
     */
    val serverDebugChannel: IChannel? by lazy { Client.getChannelByID(serverDebugChannelId) }
    /**
     * ID of Debug channel.
     */
    private val serverDebugChannelId: Long

    /**
     * IDs for bot superusers.
     */
    private var suIds by Delegates.notNull<Set<Long>>()

    /**
     * SemVer version of the bot.
     */
    private var monikaSemVersion = ""
        private set(ver) {
            field = ver
            monikaVersion = getVersion()
        }
    /**
     * The git branch of this bot.
     */
    val monikaVersionBranch: String
    /**
     * Version of the bot.
     */
    var monikaVersion by Delegates.notNull<String>()
        private set

    init {
        logger.info("${getMethodName()} - Initializing from *.properties...")

        val sourceProp = getProperties(SOURCE_PROP)

        privateKey = sourceProp.getProperty("privateKey")
        checkwxKey = sourceProp.getProperty("checkwxKey")
        ownerId = sourceProp.getProperty("adminId").toLong()
        serverDebugChannelId = sourceProp.getProperty("debugChannelId").toLong()

        loadFromSource()

        val versionProp = getProperties(VERSION_PROP)
        monikaVersionBranch = versionProp.getProperty("gitbranch")

        loadFromVersion()

        logger.info("${getMethodName()} - Done")
    }

    /**
     * @return Whether event is from the bot owner.
     */
    fun MessageEvent.isFromOwner() = author.longID == ownerId

    /**
     * @return Whether event is from a superuser.
     */
    fun MessageEvent.isFromSuperuser() = Core.suIds.any { it == author.longID }

    /**
     * Gets the method name which invoked this method.
     */
    fun getMethodName(vararg args: String): String {
        return Thread.currentThread().stackTrace[2].methodName + "(${args.joinToString(", ")})"
    }

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
     * Loads all mutable data members from source.properties.
     */
    private fun loadFromSource() {
        val prop = getProperties(SOURCE_PROP)

        suIds = loadSuIds(prop)
    }

    /**
     * Loads and formats superuser IDs.
     */
    private fun loadSuIds(prop: Properties): Set<Long> {
        return prop.getProperty("suId")
                .split(',')
                .map { it.toLong() }
                .union(listOf(ownerId))
    }

    /**
     * Loads all mutable data members from version.properties.
     */
    private fun loadFromVersion() {
        val prop = getProperties(VERSION_PROP)

        monikaSemVersion = prop.getProperty("version")
    }

    /**
     * Formats the bot's version.
     */
    private fun getVersion(): String = "$monikaSemVersion+$monikaVersionBranch"

    /**
     * Performs a full reload of the bot.
     */
    fun reload() {
        loadFromSource()
        loadFromVersion()

        PersistentMessage.modify("Misc", "Version", monikaVersion, true)
    }
}
