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
    private const val PROP_PATH = "/home/david/server/monikabot/source.properties"
    private const val PROP_FALLBACK = "/home/david/Dropbox/programming/Java_Kotlin/monikabot/source.properties"

    fun isEventFromAdmin(event: MessageEvent): Boolean {
        return event.author == Client.fetchUser(botAdmin)
    }

    fun isSudoLocationValid(event: MessageReceivedEvent): Boolean {
        return event.channel == Client.getChannelByID(debugChannel) ||
                event.channel == adminPrivateChannel
    }

    fun getArgumentList(str: String): List<String> {
        return Parser.popLeadingMention(str).popFirstWord().split(" ")
    }

    fun getDiscordTag(user: IUser): String = "${user.name}#${user.discriminator}"

    fun getChannelName(channel: IChannel): String {
        val guild = if (channel is IPrivateChannel) "[Private]" else channel.guild.name
        return "$guild/${channel.name}"
    }

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

        throw Exception("Cannot retrieve private key from source.properties")
    }

    fun getMethodName(): String {
        return Thread.currentThread().stackTrace[2].methodName + "(?)"
    }

    val botAdmin: Long = getProperties().getProperty("botAdmin").toLong()
    val adminPrivateChannel: IPrivateChannel by lazy { Client.fetchUser(botAdmin).orCreatePMChannel }
    val debugChannel = getProperties().getProperty("debugChannel").toLong()
    val privateKey = getProperties().getProperty("privateKey")
}
