package cmds

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

interface Base {
    fun handler(event: MessageReceivedEvent)
    fun handlerSudo(event: MessageReceivedEvent): Boolean
    fun help(event: MessageReceivedEvent, isSu: Boolean)
}