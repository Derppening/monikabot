package cmds

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Warframe : Base {
    override fun handler(event: MessageReceivedEvent) {
//        if ()
    }

    override fun handlerSudo(event: MessageReceivedEvent): Boolean {
        return false
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        // not handled
    }

//    private val bgTask = Timer().schedule(delay = 60000, period = 60000,
//            action = {}
//    )
}
