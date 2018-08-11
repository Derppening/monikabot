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
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object HelpTextBuilder : ILogger {
    /**
     * Builds a help text embed and sends it to [event].channel.
     *
     * @param command Command of which the help text is for.
     * @param event Event which invoked the help text.
     * @param action Actions to apply to the builder.
     */
    fun buildHelpText(command: String, event: MessageReceivedEvent, action: HelpTextHelper.() -> Unit) {
        HelpTextHelper(command, event).apply(action).send()
    }

    /**
     * Helper class for sending help texts from builders.
     */
    class HelpTextHelper(private val command: String, private val event: MessageReceivedEvent) {
        private var desc: () -> String = { "" }
        private var usages: MutableMap<String, Definitions> = mutableMapOf()

        /**
         * Description for the command.
         */
        fun description(descAction: () -> String) {
            desc = descAction
        }

        /**
         * Usage of the command.
         *
         * @param invocation Usage text.
         * @param fields Definitions for the usage text.
         */
        fun usage(invocation: String, fields: Definitions.() -> Unit = {}) {
            usages[invocation] = Definitions().apply(fields)
        }

        /**
         * Sends the help text.
         */
        fun send() {
            buildEmbed(event.channel) {
                fields {
                    withTitle("Help Text for `$command`")
                    desc().takeIf { it.isNotBlank() }?.also {
                        withDesc(it)
                    }

                    usages.toMap().forEach {
                        insertSeparator()
                        appendField("Usage", "```$command ${it.key}```", false)

                        if (it.value.desc.isNotBlank()) {
                            appendField("Description", it.value.desc, false)
                        }

                        it.value.defs.joinToString("\n") { "${it.first}: ${it.second}" }.also {
                            appendField("Options", it, false)
                        }
                    }
                }

                onError {
                    discordException { e ->
                        logToChannel(ILogger.LogLevel.ERROR, "Cannot display help text") {
                            author { event.author }
                            channel { event.channel }
                            info { e.errorMessage }
                        }
                    }
                }
            }
        }

        class Definitions {
            val defs: MutableList<Pair<String, String>> = mutableListOf()
            var desc = ""

            /**
             * Description for this command definition.
             */
            fun desc(description: () -> String) {
                desc = description()
            }

            /**
             * Definition for a term that must exist when invoking the command.
             */
            fun flag(term: String, definition: () -> String) {
                defs.add("`--$term`" to definition())
            }

            /**
             * Definition for a term that may exist when invoking the command.
             */
            fun option(term: String, definition: () -> String) {
                defs.add("`[$term]`" to definition())
            }

            /**
             * A custom definition pair.
             */
            fun field(title: String, description: () -> String) {
                defs.add(title to description())
            }
        }
    }
}