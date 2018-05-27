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

package com.derppening.monikabot.impl

import com.derppening.monikabot.core.Client
import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.models.util.ReminderDeserializer
import com.derppening.monikabot.util.helpers.ChronoHelper.dateTimeFormatter
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import com.derppening.monikabot.util.helpers.formatDuration
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToLong

object ReminderService : ILogger {
    private val timerSaveFile = Paths.get("persistent/timers.json").toUri()

    private var timers = mutableListOf<Timer>()

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

    fun schedule(args: MutableList<String>, user: IUser): String {
        if (args.isEmpty()) {
            return "Please specify how long you want the timer to be!"
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
            return e.message ?: "Unknown Exception"
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
            return "I can't set the timer!\n\nReason: ${e.message}"
        }

        val timeEndDelay = (seconds + minutes * 60 + hours * 3600 + days * 86400).roundToLong()

        val timeEnd = Instant.now().plus(timeEndDelay, ChronoUnit.SECONDS)

        val newTimer = Timer(timerName.trim(), timeEnd, user.longID)
        if (timers.any { it.isEqual(newTimer) }) {
            return "You already have a reminder with the same name!"
        }
        logger.infoFun(Core.getMethodName("...")) { "Add timer with params: name=\"${timerName.trim()}\", timeEnd=${timeEnd.toEpochMilli()}, user=${user.longID}" }
        timers.add(newTimer.apply { start() })
        exportTimersToFile()

        val expiryDateTime = dateTimeFormatter.format(timeEnd)
        val expiryDuration = Duration.between(Instant.now(), timeEnd).formatDuration()
        return "Done! Your reminder is set to expire at $expiryDateTime UTC (in $expiryDuration)."
    }

    fun clear(user: IUser): String {
        timers.filter { it.isFromUser(user) }.also {
            it.forEach { it.stop() }
            timers.removeAll(it)
        }

        return "All timers cleared!"
    }

    fun list(user: IUser): Result {
        val timers = timers.filter { it.isFromUser(user) }

        if (timers.isEmpty()) {
            return Result.Message("You have no reminders! :(")
        }

        return Result.Embed {
            withTitle("Your Reminders")

            timers.forEach {
                val expiryDateTime = dateTimeFormatter.format(it.expiryDateTime)
                val expiryDuration = Duration.between(Instant.now(), it.expiryDateTime).formatDuration()
                appendField(it.timerName, "Expires at $expiryDateTime UTC ($expiryDuration left)", false)
            }

            withTimestamp(Instant.now())
        }
    }

    fun remove(timerName: String, user: IUser): String {
        val deletedTimers = timers.filter { it.isFromUser(user) && it.timerName == timerName }.also {
            it.forEach { it.stop() }
            timers.removeAll(it)
        }

        return if (deletedTimers.isNotEmpty()) {
            exportTimersToFile()
            "Timer was successfully removed."
        } else {
            "No timer with that name was found!"
        }
    }

    @JsonDeserialize(using = ReminderDeserializer::class)
    class Timer(val timerName: String,
                val expiryDateTime: Instant,
                val userID: Long = 0L) {
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

        companion object {
            fun timerCompleteHandler(timerName: String, userID: Long) {
                buildMessage(Client.getUserByID(userID).orCreatePMChannel) {
                    content {
                        val name = if (timerName.isBlank()) "unnamed timer" else timerName
                        withContent("Your timer for $name is up!")
                    }
                }
            }

            private val timer by lazy { Timer() }
        }
    }

    sealed class Result {
        class Message(val message: String) : Result()
        class Embed(val embeds: EmbedBuilder.() -> Unit) : Result()
    }
}