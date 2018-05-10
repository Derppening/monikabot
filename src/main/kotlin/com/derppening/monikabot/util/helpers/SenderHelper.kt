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
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.RateLimitException

abstract class SenderHelper<T> : ILogger {
        var genericHandler: (Exception) -> Unit = {}
            private set
        var discordHandler: (DiscordException) -> Unit = {}
            private set

        /**
         * Sets the handler for Exception.
         */
        fun genericException(handler: (Exception) -> Unit) {
            genericHandler = handler
        }

        /**
         * Sets the handler for DiscordException.
         */
        fun discordException(handler: (DiscordException) -> Unit) {
            discordHandler = handler
        }

    /**
     * Implementation for how to send objects of type [T] to channel.
     */
    protected abstract fun sendImpl(): IMessage

    /**
     * Handlers when an exception is thrown.
     */
    fun onError(action: SenderHelper<T>.() -> Unit) {
        apply(action)
    }
    /**
     * Details on how to construct the object (if required).
     */
    protected open fun impl(action: T.() -> Unit): T = data()
    /**
     * Returns the underlying data.
     */
    abstract fun data(): T

    /**
     * Delivers an object using the method specified in [sendImpl].
     */
    private fun deliverObject(): IMessage? {
        while (true) {
            try {
                return sendImpl()
            } catch (rle: RateLimitException) {
                logger.warn("Rate Limited while trying to send Embed!")
                logger.warn("\tmethod = ${rle.method}")
                logger.warn("\tisGlobal = ${rle.isGlobal}")
                logger.warn("\ttimeout = ${rle.retryDelay}")
                Thread.sleep(rle.retryDelay)
            } catch (e: DiscordException) {
                discordHandler(e)
                e.printStackTrace()
                return null
            } catch (e: Exception) {
                genericHandler(e)
                e.printStackTrace()
                return null
            }
        }
    }

    fun send(): IMessage? = deliverObject()
}
