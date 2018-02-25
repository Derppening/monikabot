package cmds

import Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

interface Base {
    fun handler(event: MessageReceivedEvent): Parser.HandleState
    fun handlerSu(event: MessageReceivedEvent): Parser.HandleState
    fun help(event: MessageReceivedEvent, isSu: Boolean)
}