package cmds

import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder

class Echo {
    @EventSubscriber fun onReceiveMessage(event: MessageReceivedEvent) {
        if (!event.channel.isPrivate) return

        if (!event.message.content.startsWith(Client.Instance.client.ourUser.mention(false))) return

        val channel = event.channel

        val list = event.message.content.split(' ').toMutableList()
        list.removeAt(0)
        val message = list.joinToString(" ")


        try {
            MessageBuilder(Client.Instance.client)
                    .withChannel(channel)
                    .withContent(message)
                    .build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }
}