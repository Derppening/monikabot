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
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageBuilder
import sx.blah.discord.util.RateLimitException

object BuilderHelper : ILogger {
    /**
     * Helper class for sending embeds.
     */
    class EmbedHelper(val channel: IChannel) : EmbedBuilder() {
        private var genericErrorHandler: (Exception) -> Unit = {}
        private var discordErrorHandler: (DiscordException) -> Unit = {}

        /**
         * Sets the handler for Exception.
         */
        fun onGenericError(handler: (Exception) -> Unit) { genericErrorHandler = handler }

        /**
         * Sets the handler for DiscordException.
         */
        fun onDiscordError(handler: (DiscordException) -> Unit) { discordErrorHandler = handler }

        /**
         * Returns the underlying EmbedObject.
         */
        fun data(): EmbedObject {
            return build()
        }

        /**
         * Sends the underlying EmbedObject, and handle any exceptions.
         *
         * @return Message object if successfully sent, otherwise null.
         */
        fun send(): IMessage? {
            while (true) {
                try {
                    return build().let { channel.sendMessage(it) }
                } catch (rle: RateLimitException) {
                    logger.warn("Rate Limited while trying to send Embed!")
                    logger.warn("\tmethod = ${rle.method}")
                    logger.warn("\tisGlobal = ${rle.isGlobal}")
                    logger.warn("\ttimeout = ${rle.retryDelay}")
                    Thread.sleep(rle.retryDelay)
                } catch (e: DiscordException) {
                    discordErrorHandler(e)
                    e.printStackTrace()
                    return null
                } catch (e: Exception) {
                    genericErrorHandler(e)
                    e.printStackTrace()
                    return null
                }
            }
        }
    }

    /**
     * Helper class for sending messages.
     */
    class MessageHelper(channel: IChannel) : MessageBuilder(Client) {
        private var genericErrorHandler: (Exception) -> Unit = {}
        private var discordErrorHandler: (DiscordException) -> Unit = {}

        /**
         * Sets the handler for Exception.
         */
        fun onGenericError(handler: (Exception) -> Unit) { genericErrorHandler = handler }

        /**
         * Sets the handler for DiscordException.
         */
        fun onDiscordError(handler: (DiscordException) -> Unit) { discordErrorHandler = handler }

        init {
            withChannel(channel)
        }

        /**
         * Returns the underlying MessageBuilder.
         */
        fun data(): MessageBuilder {
            return this
        }

        /**
         * Sends the underlying MessageBuilder, and handle any exceptions.
         *
         * @return Message object if successfully sent, otherwise null.
         */
        override fun send(): IMessage? {
            while (true) {
                try {
                    return withChannel(channel).let { super.send() }
                } catch (rle: RateLimitException) {
                    logger.warn("Rate Limited while trying to send Embed!")
                    logger.warn("\tmethod = ${rle.method}")
                    logger.warn("\tisGlobal = ${rle.isGlobal}")
                    logger.warn("\ttimeout = ${rle.retryDelay}")
                    Thread.sleep(rle.retryDelay)
                } catch (e: DiscordException) {
                    discordErrorHandler(e)
                    e.printStackTrace()
                    return null
                } catch (e: Exception) {
                    genericErrorHandler(e)
                    e.printStackTrace()
                    return null
                }
            }
        }
    }

    fun buildEmbed(channel: IChannel, action: EmbedHelper.() -> Unit): IMessage? {
        return EmbedHelper(channel).apply(action).send()
    }

    fun buildMessage(channel: IChannel, action: MessageHelper.() -> Unit): IMessage? {
        return MessageHelper(channel).apply(action).send()
    }

    /**
     * Inserts an empty key-value field as a separator.
     */
    fun EmbedBuilder.insertSeparator(): EmbedBuilder = this.appendField("\u200B", "\u200B", false)
}