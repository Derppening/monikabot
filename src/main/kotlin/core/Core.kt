package core

import Parser
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IPrivateChannel
import sx.blah.discord.handle.obj.IUser
import java.io.File
import java.io.FileInputStream
import java.util.*

object Core {
    /**
     * Path to properties file.
     */
    private const val PROP_PATH = "/home/david/server/monikabot/source.properties"
    /**
     * Fallback path to properties file.
     */
    private const val PROP_FALLBACK = "/home/david/Dropbox/programming/Java_Kotlin/monikabot/source.properties"

    /**
     * @return Whether event is from superuser.
     */
    fun isEventFromSu(event: MessageEvent): Boolean {
        return event.author == Client.fetchUser(botAdmin)
    }

    /**
     * Whether action is performed in a superuser channel (currently only in PM or MonikaBot/debug)
     */
    fun isSudoLocationValid(event: MessageReceivedEvent): Boolean {
        return event.channel == Client.getChannelByID(debugChannel) ||
                event.channel == adminPrivateChannel
    }

    /**
     * @return List of arguments.
     */
    fun getArgumentList(str: String): List<String> {
        return Parser.popLeadingMention(str).popFirstWord().split(" ")
    }

    /**
     * @return Discord tag.
     */
    fun getDiscordTag(user: IUser): String = "${user.name}#${user.discriminator}"

    /**
     * @return Channel name in "Server/Channel" format.
     */
    fun getChannelName(channel: IChannel): String {
        val guild = if (channel is IPrivateChannel) "[Private]" else channel.guild.name
        return "$guild/${channel.name}"
    }

    /**
     * Gets the method name which invoked this method.
     */
    fun getMethodName(): String {
        return Thread.currentThread().stackTrace[2].methodName + "(?)"
    }

    /**
     * Gets the properties file.
     */
    private fun getProperties(): Properties {
        if (File(PROP_PATH).exists()) {
            return Properties().apply {
                load(FileInputStream(PROP_PATH))
            }
        }

        if (File(PROP_FALLBACK).exists())  {
            return Properties().apply {
                load(FileInputStream(PROP_FALLBACK))
            }
        }

        throw Exception("Cannot find source.properties")
    }

    /**
     * ID of bot admin.
     */
    val botAdmin: Long = getProperties().getProperty("botAdmin").toLong()
    /**
     * PM Channel of bot admin.
     */
    val adminPrivateChannel: IPrivateChannel by lazy { Client.fetchUser(botAdmin).orCreatePMChannel }
    /**
     * Debug channel.
     */
    val debugChannel = getProperties().getProperty("debugChannel").toLong()
    /**
     * Bot private key.
     */
    val privateKey = getProperties().getProperty("privateKey")
}
