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

import com.derppening.monikabot.core.Client
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.MessageBuilder

object MessageHelper {
    fun buildMessage(channel: IChannel, action: MessageHelper.() -> Unit): IMessage? {
        return MessageHelper(channel, action).send()
    }

    /**
     * Helper class for sending messages from builders.
     */
    class MessageHelper(channel: IChannel, action: MessageHelper.() -> Unit) : SenderHelper<MessageBuilder>() {
        private val builder = MessageBuilder(Client)

        init {
            action()
            builder.withChannel(channel)
        }

        override fun sendImpl(): IMessage {
            return builder.send()
        }

        fun content(action: MessageBuilder.() -> Unit): MessageBuilder = impl(action)

        override fun impl(action: MessageBuilder.() -> Unit): MessageBuilder = builder.apply(action)
        override fun data() = builder
    }
}