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
import com.derppening.monikabot.util.discordTag
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.text.StringEscapeUtils
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL

object TriviaService : ILogger {
    private val jsonMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    }

    val users = mutableListOf<Long>()

    fun startTrivia(args: List<String>, event: MessageReceivedEvent) {
        val questions = args.firstOrNull()?.toIntOrNull() ?: 5
        val difficulty = args.find { it.matches(Regex("(easy|medium|hard|any)")) } ?: "easy"

        val channel = event.author.orCreatePMChannel
        val triviaData = getTriviaQuestions(questions, difficulty)

        if (triviaData.responseCode != 0) {
            buildMessage(channel) {
                content {
                    if (triviaData.responseCode == 1) {
                        withContent("I could only ask a maximum of 50 questions at a time!")
                    } else {
                        withContent("I don't have questions to ask you... Let's play later! =3")
                    }
                }
            }

            return
        }

        logger.infoFun(Core.getMethodName()) { "Starting Trivia for ${event.author.discordTag()}" }
        buildMessage(channel) {
            content {
                withContent("Let's play Trivia! There will be $questions questions with $difficulty difficulty for you to answer.")
                appendContent("\nType \"exit\" to quit any time!")
            }
        }

        users.add(event.author.longID)

        var correctAnswers = 0
        var totalAnswers = 0

        game@ for (trivia in triviaData.results) {
            val answers = trivia.incorrectAnswers.toMutableList().also { it.add(trivia.correctAnswer) }.shuffled()
                .map { it.trim() }

            var answerDebugStr = ""
            answers.forEachIndexed { i, s ->
                answerDebugStr += "\n[$i] $s ${if (answers.indexOfFirst { it == trivia.correctAnswer.trim() } == i) "<" else ""}"
            }
            logger.debugFun(Core.getMethodName()) { "Shuffled Answers:$answerDebugStr" }

            buildEmbed(channel) {
                fields {
                    withAuthorName("Difficulty: ${trivia.difficulty.capitalize()}")
                    withTitle("Category: ${trivia.category}")
                    withDesc(StringEscapeUtils.unescapeHtml4(trivia.question))

                    answers.forEachIndexed { i, answer ->
                        appendField((i + 65).toChar().toString(), StringEscapeUtils.unescapeHtml4(answer), true)
                    }
                }
            }

            while (channel.messageHistory.latestMessage == null) {
                Thread.sleep(500)
            }

            var lastMessageId = channel.messageHistory.latestMessage.longID
            logger.debugFun(Core.getMethodName()) { "Waiting for user input for Question ${totalAnswers + 1} of $questions" }
            checkResponse@ while (true) {
                if (channel.messageHistory.latestMessage.longID != lastMessageId) {
                    val message = channel.messageHistory.latestMessage

                    lastMessageId = message.longID

                    if (message.content.equals("exit", true)) {
                        break@game
                    }

                    if (answers.any { it.equals(message.content, true) } ||
                        (message.content.length == 1 && (message.content[0].toUpperCase().toInt() - 65) in 0..answers.lastIndex)) {
                        if (answers.any { it.equals(message.content, true) }) {
                            logger.debugFun(Core.getMethodName()) {
                                "Input \"${message.content}\" matches Answer Index ${answers.indexOfFirst {
                                    it.equals(
                                        message.content,
                                        true
                                    )
                                }}"
                            }
                        } else {
                            logger.debugFun(Core.getMethodName()) {
                                "Input \"${message.content}\" converted to match Answer Index ${message.content[0].toUpperCase().toInt() - 65}"
                            }
                        }
                        break@checkResponse
                    }
                } else {
                    Thread.sleep(500)
                }
            }

            val ans = try {
                channel.messageHistory.latestMessage.content ?: throw Exception("Latest message is a NullPointer")
            } catch (e: Exception) {
                buildMessage(event.channel) {
                    content {
                        withContent("Monika hit a hiccup and needs to take a break :(")
                    }
                }

                e.printStackTrace()
                break@game
            }

            when (trivia.type) {
                "boolean" -> {
                    when {
                        ans.toBoolean() == trivia.correctAnswer.toBoolean() ||
                                ans.length == 1 && (ans[0].toUpperCase().toInt() - 65) == answers.indexOfFirst { it == trivia.correctAnswer } -> {
                            buildMessage(channel) {
                                content {
                                    withContent("You are correct! =D")
                                }
                            }
                            ++correctAnswers
                        }
                        else -> {
                            buildMessage(channel) {
                                content {
                                    withContent("You're incorrect... :(\nThe correct answer is ${trivia.correctAnswer}.")
                                }
                            }
                        }
                    }
                }
                "multiple" -> {
                    when {
                        ans.equals(trivia.correctAnswer.trim(), true) ||
                                ans.length == 1 && (ans[0].toUpperCase().toInt() - 65) == answers.indexOfFirst { it == trivia.correctAnswer } -> {
                            buildMessage(channel) {
                                content {
                                    withContent("You are correct! =D")
                                }
                            }
                            ++correctAnswers
                        }
                        else -> {
                            buildMessage(channel) {
                                content {
                                    withContent(
                                        "You're incorrect... :(\nThe correct answer is ${StringEscapeUtils.unescapeHtml4(
                                            trivia.correctAnswer
                                        )}."
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ++totalAnswers
        }

        buildMessage(channel) {
            content {
                withContent("Thanks for playing trivia with me! You got $correctAnswers out of $totalAnswers correct!")
            }
        }

        users.remove(event.author.longID)
        logger.infoFun(Core.getMethodName()) { "Ending Trivia for ${event.author.discordTag()}" }
    }

    private fun getTriviaQuestions(questions: Int, difficulty: String): TriviaData {
        return jsonMapper.readValue(URL("https://opentdb.com/api.php?amount=$questions${if (difficulty != "any") "&difficulty=$difficulty" else ""}"))
    }

    /**
     * Checks whether the current user is playing trivia.
     */
    fun checkUserTriviaStatus(event: MessageReceivedEvent): Boolean {
        if (users.any { it == event.author.longID }) {
            if (!event.channel.isPrivate) {
                buildMessage(event.channel) {
                    content {
                        withContent("It looks like you're still in a trivia game... Type \"exit\" in my private chat to quit it!")
                    }
                }
            }
            return true
        }

        return false
    }

    fun gracefulShutdown() {
        users.forEach {
            val channel = Client.getUserByID(it)!!.orCreatePMChannel
            buildMessage(channel) {
                content {
                    withContent("Friendly Reminder: I will be going down for maintenance in one minute!")
                }
            }
        }
    }

    class TriviaData {
        @JsonProperty("response_code")
        val responseCode = 0
        val results = listOf<Result>()

        class Result {
            val category = ""
            val type = ""
            val difficulty = ""
            val question = ""
            @JsonProperty("correct_answer")
            val correctAnswer = ""
            @JsonProperty("incorrect_answers")
            val incorrectAnswers = listOf<String>()
        }
    }
}