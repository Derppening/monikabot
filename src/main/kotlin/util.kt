import sx.blah.discord.handle.obj.IUser
import java.io.FileInputStream
import java.util.Properties

internal const val PROP_PATH = "/home/david/server/monikabot/source.properties"

fun getBotAdmin(): Long {
    val prop = Properties()
    prop.load(FileInputStream(PROP_PATH))

    return prop.getProperty("botAdmin").toLong()
}

fun getPrivateKey(): String {
    val prop = Properties()
    prop.load(FileInputStream(PROP_PATH))

    return prop.getProperty("privateKey") ?: throw Exception("Cannot retrieve private key from source.properties!")
}