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

package com.derppening.monikabot.commands

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.impl.ToiletService.toASCIIText
import com.derppening.monikabot.impl.ToiletService.toEmojiText
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Toilet : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        when {
            args.isNotEmpty() && args[0] == "--emoji" -> {
                buildMessage(event.channel) {
                    content {
                        withContent(args.drop(1).joinToString(" ").toEmojiText())
                    }
                }
            }
            args.size == 1 && args[0] == "--font" -> {
                buildMessage(event.channel) {
                    content {
                        withContent("http://artii.herokuapp.com/fonts_list")
                    }
                }
            }
            else -> {
                val font = args.firstOrNull()?.takeIf { it.startsWith("--font=") }?.removePrefix("--font=")
                val text = args.dropWhile { it.startsWith("--font=") }.joinToString(" ")

                buildMessage(event.channel) {
                    content {
                        text.toASCIIText(font).also {
                            if (it.length >= 1990) {
                                withContent("Message is too long to be reformatted!")
                            } else {
                                withContent("```${text.toASCIIText(font)}```")
                            }
                        }
                    }
                }
            }
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText("toilet", event) {
            description { "Formats text to different styles." }

            usage("toilet [--font=font] [text]") {
                def("[font]") { "Font to use." }
                def("[text]") { "Text to reformat." }
            }

            usage("toilet --emoji [text]") {
                def("[text]") { "Text to format using emojis." }
            }

            usage("toilet --font") {
                def("--font") { "Lists all fonts available." }
            }
        }
    }
}