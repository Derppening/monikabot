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

package com.derppening.monikabot.controller.commands

import com.derppening.monikabot.controller.CommandInterpreter
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.ToiletService.toASCIIText
import com.derppening.monikabot.impl.ToiletService.toEmojiText
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Toilet : IBase, ILogger {
    override fun cmdName(): String = "toilet"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = getArgumentList(event.message.content)

        when {
            args.isNotEmpty() && args[0] == "--emoji" -> {
                buildMessage(event.channel) {
                    content {
                        convertToEmoji(args.drop(1).joinToString(" ")).also {
                            withContent(it)
                        }
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
                        convertToASCII(text, font).also {
                            if (it.length >= 1990) {
                                withContent("Message is too long to be reformatted!")
                            } else {
                                withContent("```$it```")
                            }
                        }
                    }
                }
            }
        }

        return CommandInterpreter.HandleState.HANDLED
    }

    private fun convertToEmoji(text: String): String = text.toEmojiText()

    private fun convertToASCII(text: String, font: String?): String = text.toASCIIText(font)

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildHelpText(cmdInvocation(), event) {
            description { "Formats text to different styles." }

            usage("[--font=FONT] [TEXT]") {
                option("FONT") { "Font to use." }
                option("TEXT") { "Text to reformat." }
            }

            usage("--emoji [TEXT]") {
                desc { "Formats the text using emojis." }

                option("TEXT") { "Text to format." }
            }

            usage("--font") {
                desc { "Lists all fonts available." }
            }
        }
    }
}