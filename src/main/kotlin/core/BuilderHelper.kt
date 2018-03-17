/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        val embed = buildEmbed(action)
        while (true) {
            try {
                return embed.let { channel.sendMessage(it) }
            } catch (rle: RateLimitException) {
                Thread.sleep(rle.retryDelay)
            }
        }
    }

    /**
     * A helper for building and sending messages.
     *
     * @param channel Channel to send the message into.
     * @param action The actions to be applied to message builder.
     *
     * @return The message of the message?
     */
    fun buildMessage(channel: IChannel, action: MessageBuilder.() -> Unit): IMessage {
        val message = MessageBuilder(Client).withChannel(channel).apply(action)
        while (true) {
            try {
                return message.build()
            } catch (rle: RateLimitException) {
                Thread.sleep(rle.retryDelay)
            }
        }
    }
}