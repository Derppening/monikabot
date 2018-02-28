package core

import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageBuilder

object BuilderHelper {
    fun buildEmbed(action: EmbedBuilder.() -> Unit): EmbedObject {
        return EmbedBuilder().apply(action).build()
    }

    fun buildEmbed(channel: IChannel, action: EmbedBuilder.() -> Unit): IMessage {
        return buildEmbed(action).let { channel.sendMessage(it) }
    }

    fun buildMessage(channel: IChannel, action: MessageBuilder.() -> Unit): IMessage {
        return MessageBuilder(Client).withChannel(channel).apply(action).build()
    }
}