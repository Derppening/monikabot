import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.DiscordException
import java.io.FileInputStream
import java.util.*
import kotlin.system.exitProcess

fun createClient(token: String, login: Boolean): IDiscordClient {
    val builder = ClientBuilder()
    builder.withToken(token)
    try {
        return when (login) {
            true -> builder.login()
            false -> builder.build()
        }
    } catch (e: DiscordException) {
        e.printStackTrace()
        exitProcess(0)
    }
}

fun getProperties(): Properties {
    val propPath = Thread.currentThread().contextClassLoader.getResource("").path + "../../../../source.properties"
    val prop = Properties()
    prop.load(FileInputStream(propPath))
    return prop
}

fun main(args: Array<String>) {
    val client = createClient(getProperties().getProperty("privatekey"), true)
    val dispatcher = client.dispatcher ?: exitProcess(0)
}