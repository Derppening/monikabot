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

package cmds

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.Client
import core.Core
import core.ILogger
import core.Parser
import models.util.ReminderDeserializer
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToLong

object Reminder : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        when {
            args.isEmpty() -> help(event, false)
            args.any { it.matches(Regex("-{0,2}add")) } -> scheduleDelay(event)
            args.any { it.matches(Regex("-{0,2}list")) } -> listTimer(event)
            args.any { it.matches(Regex("-{0,2}remove")) } -> removeTimer(event)
            args.any { it.matches(Regex("-{0,2}clear")) } -> clearTimers(event)
            args.any { it.matches(Regex("for")) } -> scheduleDelay(event)
            else -> help(event, false)
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `reminder`")
            withDesc("Sets a reminder for yourself.")
            appendDesc("\n**WARNING**: Do not use this timer for any mission-critical tasks. When this bot goes" +
                    "into maintenance, all timer tasks will be paused until the bot restarts. This will likely cause" +
                    "reminder delays!")
            insertSeparator()
            appendField("Usage", "```reminder for [--lazy] [duration] [name]```", false)
            appendField("`--lazy`", "If specified, only check if the time is in the future.", false)
            appendField("`[duration]`", "Any duration, in the format of `[days]d [hours]h [minutes]m [seconds]s`." +
                    "\nAny part of the duration can be truncated.", false)
            appendField("`[name]`", "Name of the timer. All timers must have unique names.", false)
            insertSeparator()
            appendField("Usage", "```reminder remove [name]```", false)
            appendField("`[name]`", "Name of the timer to remove.", false)
            insertSeparator()
            appendField("Usage", "```reminder [list|clear]```", false)
            appendField("`list`", "Lists all ongoing reminders.", false)
            appendField("`clear`", "Clears all ongoing reminders.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    fun exportTimersToFile() {
        logger.debug("${Core.getMethodName()} -> ${timerSaveFile.path}")

        jacksonObjectMapper().writeValue(File(timerSaveFile), timers)
    }

    fun importTimersFromFile() {
        logger.debug("${Core.getMethodName()} <- ${timerSaveFile.path}")

        timers = jacksonObjectMapper().readValue(Paths.get(timerSaveFile).toFile())
        timers.filter { Duration.between(Instant.now(), it.expiryDateTime) < Duration.ZERO }.forEach {
            Timer.timerCompleteHandler(it.timerName, it.userID)
            timers.remove(it)
        }
        timers.forEach {
            it.start()
        }
    }

    private fun scheduleDelay(event: MessageReceivedEvent) {
        val args = getArgumentList(event.message.content)
                .toMutableList()
                .apply { removeIf { it.matches(Regex("(for|-{0,2}add)")) } }

        if (args.isEmpty()) {
            buildMessage(event.channel) {
                withContent("Please specify how long you want the timer to be!")
            }

            return
        }

        val isLazy = args.any { it.matches(Regex("-{0,2}lazy")) }.also {
            if (it) {
                args.removeIf { it.matches(Regex("-{0,2}lazy")) }
            }
        }

        val (_, dayString, _, hourString, _, minuteString, _, secondString, timerName) = try {
            val numRegex = Regex("\\d+(?:.\\d+)?")
            Regex("^(($numRegex)d)?\\s*(($numRegex)h)?\\s*(($numRegex)m)?\\s*(($numRegex)s)?\\s*?(.*)", RegexOption.DOT_MATCHES_ALL)
                    .matchEntire(args.joinToString(" "))
                    ?.destructured
                    ?: error("Timer duration is not formatted properly!")
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent(e.message)
            }

            return
        }

        val days = dayString.toDoubleOrNull() ?: 0.0
        val hours = hourString.toDoubleOrNull() ?: 0.0
        val minutes = minuteString.toDoubleOrNull() ?: 0.0
        val seconds = secondString.toDoubleOrNull() ?: 0.0

        try {
            check(days >= 0) { "You cannot set a reminder for the past!" }
            check(hours >= 0) { "You cannot set a reminder for the past!" }
            check(minutes >= 0) { "You cannot set a reminder for the past!" }
            check(seconds >= 0) { "You cannot set a reminder for the past!" }

            if (!isLazy) {
                check(hours < 24) { "Strict Checking: Hours cannot be over 23!" }
                check(minutes < 60) { "Strict Checking: Minutes cannot be over 59!" }
                check(seconds < 60) { "Strict Checking: Minutes cannot be over 59!" }
            }
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent("I can't set the timer!")
                appendContent("\n\nReason: ${e.message}")
            }

            return
        }

        val timeEndDelay = (seconds + minutes * 60 + hours * 3600 + days * 86400).roundToLong()

        val timeEnd = Instant.now().plus(timeEndDelay, ChronoUnit.SECONDS)

        val newTimer = Timer(timerName.trim(), timeEnd, event.author.longID)
        if (timers.any { it.isEqual(newTimer) }) {
            buildMessage(event.channel) {
                withContent("You already have a reminder with the same name!")
            }
            return
        }
        timers.add(newTimer.apply { start() })
        exportTimersToFile()

        buildMessage(event.channel) {
            val expiryDateTime = dateTimeFormatter.format(timeEnd)
            val expiryDuration = formatTimeDuration(Duration.between(Instant.now(), timeEnd))
            withContent("Done! Your reminder is set to expire at $expiryDateTime UTC (in $expiryDuration).")
        }
    }

    private fun clearTimers(event: MessageReceivedEvent) {
        timers.filter { it.isFromUser(event.author) }.also {
            it.forEach { it.stop() }
            timers.removeAll(it)
        }

        buildMessage(event.author.orCreatePMChannel) {
            withContent("All timers cleared!")
        }
    }

    private fun listTimer(event: MessageReceivedEvent) {
        val timers = timers.filter { it.isFromUser(event.author) }

        if (timers.isEmpty()) {
            buildMessage(event.author.orCreatePMChannel) {
                withContent("You have no reminders! :(")
            }
            return
        }

        buildEmbed(event.author.orCreatePMChannel) {
            withTitle("Your Reminders")

            timers.forEach {
                val expiryDateTime = dateTimeFormatter.format(it.expiryDateTime)
                val expiryDuration = formatTimeDuration(Duration.between(Instant.now(), it.expiryDateTime))
                appendField(it.timerName, "Expires at $expiryDateTime UTC ($expiryDuration left)", false)
            }

            withTimestamp(Instant.now())
        }
    }

    private fun removeTimer(event: MessageReceivedEvent) {
        val timerName = getArgumentList(event.message.content)
                .toMutableList()
                .apply { removeIf { it.matches(Regex("-{0,2}remove")) } }
                .joinToString(" ")

        val deletedTimers = timers.filter { it.isFromUser(event.author) && it.timerName == timerName }.also {
            it.forEach { it.stop() }
            timers.removeAll(it)
        }
        buildMessage(event.channel) {
            if (deletedTimers.isNotEmpty()) {
                withContent("Timer was successfully removed.")
                exportTimersToFile()
            } else {
                withContent("No timer with that name was found!")
            }
        }
    }

    private fun formatTimeDuration(duration: Duration): String {
        return (if (duration.toDays() > 0) "${duration.toDays()}d " else "") +
                (if (duration.toHours() % 24 > 0) "${duration.toHours() % 24}h " else "") +
                (if (duration.toMinutes() % 60 > 0) "${duration.toMinutes() % 60}m " else "") +
                "${duration.seconds % 60}s"
    }

    private var timers = mutableListOf<Timer>()

    private val dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"))

    @JsonDeserialize(using = ReminderDeserializer::class)
    class Timer(internal val timerName: String,
                internal val expiryDateTime: Instant,
                internal val userID: Long = 0L) {

        companion object {
            fun timerCompleteHandler(timerName: String, userID: Long) {
                buildMessage(Client.getOrCreatePMChannel(Client.getUserByID(userID))) {
                    val name = if (timerName.isBlank()) "unnamed timer" else timerName
                    withContent("Your timer for $name is up!")
                }
            }

            private val timer by lazy { Timer() }
        }

        private val task = timerTask {
            timerCompleteHandler(timerName, userID)
            cancel()
            timers.remove(this@Timer)
        }

        fun isEqual(other: Timer): Boolean {
            return userID == other.userID && timerName == other.timerName
        }

        fun isFromUser(user: IUser): Boolean {
            return user.longID == this.userID
        }

        fun start() {
            timer.schedule(task, Date.from(expiryDateTime))
        }

        fun stop() {
            task.cancel()
        }

    }

    private val timerSaveFile = Paths.get("persistent/timers.json").toUri()

}