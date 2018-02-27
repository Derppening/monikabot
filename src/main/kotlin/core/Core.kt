package core

import Parser
import popFirstWord
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
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
     * Whether action is performed in a superuser channel (currently only in PM or MonikaBot/debug)
     */
    fun isSudoLocationValid(event: MessageEvent) =
            event.channel == serverDebugChannel || event.channel == adminPrivateChannel

    /**
     * @return Whether event is from superuser.
     */
    fun isEventFromSuperuser(event: MessageEvent) = event.author == Client.fetchUser(botAdminId)

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
     * PM Channel of bot admin.
     */
    val adminPrivateChannel: IPrivateChannel by lazy { Client.fetchUser(botAdminId).orCreatePMChannel }
    /**
     * Debug channel.
     */
    val serverDebugChannel: IChannel? by lazy { Client.getChannelByID(serverDebugChannelId) }
    /**
     * Bot private key.
     */
    val privateKey = getProperties(SOURCE_PROP).getProperty("privateKey")!!
    /**
     * Version of the bot.
     */
    val monikaVersion = getProperties(VERSION_PROP).getProperty("version")!!

    /**
     * ID of bot admin.
     */
    private val botAdminId = getProperties(SOURCE_PROP).getProperty("botAdminId").toLong()
    /**
     * ID of Debug channel.
     */
    private val serverDebugChannelId = getProperties(SOURCE_PROP).getProperty("debugChannelId").toLong()
}
