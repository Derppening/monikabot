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

package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.insertSeparator
import core.Client
import core.Core
import core.IChannelLogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import kotlin.system.exitProcess

object Stop : IBase, IChannelLogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        if (!Core.isEventFromOwner(event)) {
            return Parser.HandleState.PERMISSION_DENIED
        } else if (!Core.isOwnerLocationValid(event)) {
            return Parser.HandleState.UNHANDLED
        }

        val args = getArgumentList(event.message.content)
        if (args.any { it.matches(Regex("-{0,2}dev(elopment)?")) } && Core.monikaVersionBranch != "development") {
            return Parser.HandleState.HANDLED
        }
        if (args.any { it.matches(Regex("-{0,2}stable")) } && Core.monikaVersionBranch != "stable") {
            return Parser.HandleState.HANDLED
        }

        log(IChannelLogger.LogLevel.WARN, "Logging out with ${event.client.shardCount} shard(s) active") {
            author { event.author }
            channel { event.channel}
        }

        Client.clearTimers()

        event.client.shards.forEach {
            it.logout()
        }
        exitProcess(0)
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        if (isSu) {
            try {
                buildEmbed(event.channel) {
                    withTitle("Help Text for `stop`")
                    withDesc("Stops the execution of the bot.")
                    insertSeparator()
                    appendField("Usage", "```stop [stable|development]```", false)
                    appendField("`[stable|development]`", "Optional: Which specific instance(s) to stop.", false)
                }
            } catch (e: DiscordException) {
                log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
                e.printStackTrace()
            }
        }
    }
}
