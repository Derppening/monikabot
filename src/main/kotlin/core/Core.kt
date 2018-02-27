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
import java.io.FileNotFoundException
import java.util.*
import kotlin.system.exitProcess

object Core {
    /**
     * Filename of source.properties.
     */
    private const val SOURCE_PROP = "source.properties"
    /**
     * Filename of version.properties.
     */
    private const val VERSION_PROP = "version.properties"

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
     * ID of bot admin.
     */
    val botAdmin: Long = getProperties(SOURCE_PROP).getProperty("botAdmin").toLong()
    /**
     * PM Channel of bot admin.
     */
    val adminPrivateChannel: IPrivateChannel by lazy { Client.fetchUser(botAdmin).orCreatePMChannel }
    /**
     * Debug channel.
     */
    val debugChannel = getProperties(SOURCE_PROP).getProperty("debugChannel").toLong()
    /**
     * Bot private key.
     */
    val privateKey = getProperties(SOURCE_PROP).getProperty("privateKey")!!
    /**
     * Version of the bot.
     */
    val monikaVersion = getProperties(VERSION_PROP).getProperty("version")!!
}
