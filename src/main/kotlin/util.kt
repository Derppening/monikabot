import core.Client
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IPrivateChannel
import sx.blah.discord.handle.obj.IUser
import java.io.FileInputStream
import java.util.*

private const val PROP_PATH = "/home/david/server/monikabot/source.properties"

fun getDiscordTag(user: IUser): String = "${user.name}#${user.discriminator}"

fun getChannelId(channel: IChannel): String {
    val guild = if (channel is IPrivateChannel) "[Private]" else channel.guild.name
    return "$guild/${channel.name}"
}

fun String.popFirstWord(): String = dropWhile { it != ' ' }.dropWhile { it == ' ' }

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
