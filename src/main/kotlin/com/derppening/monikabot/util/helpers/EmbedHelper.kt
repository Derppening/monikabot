/*
 *  This file is part of MonikaBot.
 *
 *  Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 *  MonikaBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MonikaBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.derppening.monikabot.util.helpers

import com.derppening.monikabot.core.ILogger
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IEmbed
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder

object EmbedHelper : ILogger {
    /**
     * Helper class for sending embeds from builders.
     */
    class EmbedHelper(val channel: IChannel, action: EmbedHelper.() -> Unit) : SenderHelper<EmbedBuilder>() {
        private val builder = EmbedBuilder()

        init {
            action()
        }

        override fun sendImpl(): IMessage {
            return channel.sendMessage(this.toEmbedObject())
        }

        fun fields(action: EmbedBuilder.() -> Unit): EmbedBuilder = impl(action)

        override fun impl(action: EmbedBuilder.() -> Unit): EmbedBuilder = builder.apply(action)
        override fun data(): EmbedBuilder = builder

        /**
         * Builds and returns the underlying EmbedObject.
         */
        fun toEmbedObject(): EmbedObject = builder.build()
    }

    /**
     * Helper class for sending embeds.
     */
    class EmbedSender(val channel: IChannel, private val embed: EmbedObject, action: EmbedSender.() -> Unit) : SenderHelper<EmbedObject>() {
        init {
            action()
        }

        override fun sendImpl(): IMessage {
            return channel.sendMessage(embed)
        }

        override fun data(): EmbedObject = embed
    }

    fun buildEmbed(channel: IChannel, action: EmbedHelper.() -> Unit): IMessage? {
        return EmbedHelper(channel, action).send()
    }

    fun sendEmbed(channel: IChannel, embed: EmbedObject, action: EmbedSender.() -> Unit = {}): IMessage? {
        return EmbedSender(channel, embed, action).send()
    }

    fun sendEmbed(channel: IChannel, builder: EmbedBuilder, action: EmbedSender.() -> Unit = {}): IMessage? {
        return sendEmbed(channel, builder.build(), action)
    }

    /**
     * Converts into an EmbedObjecet, copying all impl from the original.
     *
     * @param action Fields
     */
    fun IEmbed.toEmbedObject(action: EmbedBuilder.() -> Unit): EmbedObject {
        return EmbedBuilder().apply {
            author?.name?.also { withAuthorName(it) }
            author?.iconUrl?.also { withAuthorIcon(it) }
            author?.url?.also { withAuthorUrl(it) }
            title?.also { withTitle(it) }
            description?.also { withDesc(it) }

            embedFields?.forEach {
                appendField(it)
            }

            url?.also { withUrl(it) }
            footer?.text?.also { withFooterText(it) }
            footer?.iconUrl?.also { withFooterIcon(it) }
            timestamp?.also { withTimestamp(it) }

            image?.url?.also { withImage(it) }
            thumbnail?.url?.also { withThumbnail(it) }

            color.also { withColor(it) }
        }.apply { action() }.build()
    }

    /**
     * Inserts an empty key-value field as a separator.
     */
    fun EmbedBuilder.insertSeparator(): EmbedBuilder = this.appendField("\u200B", "\u200B", false)
}