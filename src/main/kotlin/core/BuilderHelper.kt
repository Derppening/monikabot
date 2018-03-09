package core

import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageBuilder
import sx.blah.discord.util.RateLimitException

object BuilderHelper {
    /**
     * A helper for building embeds.
     *
     * @param action The actions to be applied to the embed builder.
     *
     * @return An EmbedObject.
     */
    fun buildEmbed(action: EmbedBuilder.() -> Unit): EmbedObject {
        return EmbedBuilder().apply(action).build()
    }

    /**
     * A helper for building and sending embeds.
     *
     * @param channel Channel to send the embed into.
     * @param action The actions to be applied to the embed builder.
     *
     * @return The message of the embed.
     */
    fun buildEmbed(channel: IChannel, action: EmbedBuilder.() -> Unit): IMessage {
        while (true) {
            try {
                return buildEmbed(action).let { channel.sendMessage(it) }
            } catch (rle: RateLimitException) {
                Thread.sleep(rle.retryDelay)
            }
        }
    }

    fun buildMessage(channel: IChannel, action: MessageBuilder.() -> Unit): IMessage {
        while (true) {
            try {
                return MessageBuilder(Client).withChannel(channel).apply(action).build()
            } catch (rle: RateLimitException) {
                Thread.sleep(rle.retryDelay)
            }
        }
    }
}