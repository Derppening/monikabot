package cmds.experimental

import cmds.IBase
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.Parser
import org.apache.commons.text.StringEscapeUtils
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL
import kotlin.concurrent.thread

object Trivia : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val channel = event.author.orCreatePMChannel
        val triviaData = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.readValue<TriviaData>(URL("https://opentdb.com/api.php?amount=5"))

        if (triviaData.responseCode != 0) {
            buildMessage(channel) {
                if (triviaData.responseCode == 1) {
                    withContent("I could only ask a maximum of 50 questions at a time!")
                } else {
                    withContent("I don't have questions to ask you... Let's play later! =3")
                }
            }

            return Parser.HandleState.HANDLED
        }

        buildMessage(channel) {
            withContent("Let's play Trivia!")
        }

        thread {
            users.add(event.author.longID)

            var correctAnswers = 0
            var totalAnswers = 0

            game@ for (trivia in triviaData.results) {
                val answers = trivia.incorrectAnswers.toMutableList().also { it.add(trivia.correctAnswer) }.shuffled()
                val embed = buildEmbed(channel) {
                    withAuthorName("Difficulty: ${trivia.difficulty.capitalize()}")
                    withTitle("Category: ${trivia.category}")
                    withDesc(StringEscapeUtils.unescapeHtml4(trivia.question))


                    answers.forEachIndexed { i, answer ->
                        appendField((i + 65).toChar().toString(), StringEscapeUtils.unescapeHtml4(answer), true)
                    }
                }


                var lastMessageId = embed.longID
                checkResponse@ while (true) {
                    if (channel.messageHistory.latestMessage.longID != lastMessageId) {
                        lastMessageId = channel.messageHistory.latestMessage.longID

                        if (channel.messageHistory.latestMessage.content.toLowerCase() == "exit") {
                            break@game
                        }

                        when (trivia.type) {
                            "boolean" -> {
                                break@checkResponse
                            }
                            "multiple" -> {
                                if (answers.any { it.toLowerCase() == channel.messageHistory.latestMessage.content.toLowerCase() } ||
                                        (channel.messageHistory.latestMessage.content.length == 1 && (channel.messageHistory.latestMessage.content[0].toInt() - 65) < 4)) {
                                    break@checkResponse
                                }
                            }
                        }
                    } else {
                        Thread.sleep(500)
                    }
                }

                val ans = channel.messageHistory.latestMessage.content!!
                when (trivia.type) {
                    "boolean" -> {
                        when {
                            ans.toBoolean() == trivia.correctAnswer.toBoolean() -> {
                                buildMessage(channel) {
                                    withContent("You are correct! =D")
                                }
                                ++correctAnswers
                            }
                            else -> {
                                buildMessage(channel) {
                                    withContent("You're incorrect... :(\nThe correct answer is ${trivia.correctAnswer}")
                                }
                            }
                        }
                    }
                    "multiple" -> {
                        when {
                            ans.toLowerCase() == trivia.correctAnswer.toLowerCase() ||
                                    ans.length == 1 && (ans[0].toInt() - 65) == answers.indexOfFirst { it == trivia.correctAnswer } -> {
                                buildMessage(channel) {
                                    withContent("You are correct! =D")
                                }
                                ++correctAnswers
                            }
                            else -> {
                                buildMessage(channel) {
                                    withContent("You're incorrect... :(\nThe correct answer is ${trivia.correctAnswer}")
                                }
                            }
                        }
                    }
                }

                ++totalAnswers
            }

            buildMessage(channel) {
                withContent("Thanks for playing trivia with me! You got $correctAnswers out of $totalAnswers correct!")
            }

            users.remove(event.author.longID)
        }

        return Parser.HandleState.HANDLED
    }

    var users = mutableListOf<Long>()
        private set

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