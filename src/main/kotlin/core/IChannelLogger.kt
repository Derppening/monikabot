package core

import core.BuilderHelper.buildEmbed
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import java.awt.Color

interface IChannelLogger {
    enum class LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    class LogHelper(val type: LogLevel, val message: String, val clazz: Class<*>) {
        private var srcMessage: () -> IMessage? = { null }
        private var srcAuthor: () -> IUser? = { null }
        private var srcChannel: () -> IChannel? = { null }
        private var info: () -> String = { "" }

        fun message(action: () -> IMessage) { srcMessage = action }
        fun author(action: () -> IUser) { srcAuthor = action }
        fun channel(action: () -> IChannel) { srcChannel = action }
        fun info(action: () -> String) { info = action }

        fun build() {
            buildEmbed(PersistentMessage) {
                when (type) {
                    LogLevel.DEBUG -> {
                        withColor(Color.BLACK)
                        withTitle("Debug")
                    }
                    LogLevel.INFO -> {
                        withColor(Color.GRAY)
                        withTitle("Info")
                    }
                    LogLevel.WARN -> {
                        withColor(Color.YELLOW)
                        withTitle("Warning")
                    }
                    LogLevel.ERROR -> {
                        withColor(Color.RED)
                        withTitle("Error")
                    }
                }

                withDesc(message)

                if (srcMessage() != null) appendField("Caused by", "`${srcMessage()?.content}`", false)
                if (srcAuthor() != null) appendField("From", Core.getDiscordTag(srcAuthor()!!), false)
                if (srcChannel() != null) appendField("In", Core.getChannelName(srcChannel()!!), false)
                if (info().isNotBlank()) appendField("Additional Info", info(), false)

                withFooterText("Package: ${clazz.name}")
            }
        }
    }

    fun log(level: LogLevel, message: String, action: LogHelper.() -> Unit = {}) {
        LogHelper(level, message, this.javaClass).apply(action).build()
    }
}
