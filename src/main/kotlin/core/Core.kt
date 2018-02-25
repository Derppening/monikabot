package core

import Parser
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IPrivateChannel
import sx.blah.discord.handle.obj.IUser
import java.io.FileInputStream
import java.util.*

object Core {
    private const val PROP_PATH = "/home/david/server/monikabot/source.properties"

    fun isEventFromAdmin(event: MessageEvent): Boolean {
        return event.author == Client.fetchUser(getBotAdmin())
    }

    fun isSudoLocationValid(event: MessageReceivedEvent): Boolean {
        return event.channel == Client.getChannelByID(Core.getDebugChannel()) ||
                event.channel == Core.getAdminPrivateChannel()
    }

    fun getArgumentList(str: String): List<String> {
        return Parser.popLeadingMention(str).popFirstWord().split(" ")
    }

    fun getDiscordTag(user: IUser): String = "${user.name}#${user.discriminator}"

    fun getChannelName(channel: IChannel): String {
        val guild = if (channel is IPrivateChannel) "[Private]" else channel.guild.name
        return "$guild/${channel.name}"
    }

    fun getBotAdmin(): Long = Properties().apply {
        load(FileInputStream(PROP_PATH))
    }.getProperty("botAdmin").toLong()

    fun getAdminPrivateChannel(): IPrivateChannel = Client.fetchUser(getBotAdmin()).orCreatePMChannel

    fun getDebugChannel(): Long = Properties().apply {
        load(FileInputStream(PROP_PATH))
    }.getProperty(("debugChannel")).toLong()

    fun getPrivateKey(): String = Properties().apply {
        load(FileInputStream(PROP_PATH))
    }.getProperty("privateKey") ?: throw Exception("Cannot retrieve private key from source.properties")
}
