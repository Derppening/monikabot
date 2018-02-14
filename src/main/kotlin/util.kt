import java.io.FileInputStream
import java.util.*

private const val PROP_PATH = "/home/david/server/monikabot/source.properties"

fun String.popFirstWord(): String = dropWhile { it != ' ' }

fun getBotAdmin(): Long = Properties().apply {
    load(FileInputStream(PROP_PATH))
}.getProperty("botAdmin").toLong()

fun getDebugChannel(): Long = Properties().apply {
    load(FileInputStream(PROP_PATH))
}.getProperty(("debugChannel")).toLong()

fun getPrivateKey(): String = Properties().apply {
    load(FileInputStream(PROP_PATH))
}.getProperty("privateKey") ?: throw Exception("Cannot retrieve private key from source.properties")
