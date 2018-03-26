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
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import kotlin.system.exitProcess

object Stop : IBase, ILogger {
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

        val isForced = args.any { it.matches(Regex("-{0,2}force")) }
        if (!isForced) {
            Client.changePresence(StatusType.DND, ActivityType.PLAYING, "Maintenance")
            if (Trivia.users.isNotEmpty()) {
                log(ILogger.LogLevel.INFO, "Sending shutdown messages to all Trivia players...")
                Trivia.gracefulShutdown()
            }
            Thread.sleep(60000)
        }

        Reminder.exportTimersToFile()
        Client.clearTimers()

        event.client.shards.forEach {
            it.logout()
        }
        exitProcess(0)
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        if (isSu) {
            buildEmbed(event.channel) {
                withTitle("Help Text for `stop`")
                withDesc("Stops the execution of the bot.")
                insertSeparator()
                appendField("Usage", "```stop [stable|development]```", false)
                appendField("`[stable|development]`", "Optional: Which specific instance(s) to stop.", false)

                onDiscordError { e ->
                    log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                        author { event.author }
                        channel { event.channel }
                        info { e.errorMessage }
                    }
                }
            }
        }
    }
}
