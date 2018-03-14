package cmds.experimental

import cmds.IBase
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.Core
import core.IChannelLogger
import core.IConsoleLogger
import core.Parser
import insertSeparator
import org.apache.commons.text.StringEscapeUtils
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.net.URL
import kotlin.concurrent.thread

object Trivia : IBase, IChannelLogger, IConsoleLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).dropWhile {
            it == "--experimental"
        }

        val questions = if (args.isNotEmpty() && args[0].toIntOrNull() != null) {
            args[0].toInt()
        } else {
            5
        }
        val difficulty = if (args.isNotEmpty() && args.any { it.matches(Regex("(easy|medium|hard|any)"))} ) {
            args.find { it.matches(Regex("(easy|medium|hard|any)")) }
        } else {
            "easy"
        }

        val channel = event.author.orCreatePMChannel
        val triviaData = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.readValue<TriviaData>(URL("https://opentdb.com/api.php?amount=$questions${if (difficulty != "any") "&difficulty=$difficulty" else ""}"))

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
            withContent("Let's play Trivia! There will be $questions questions with $difficulty difficulty for you to answer.")
            appendContent("\nType \"exit\" to quit any time!")
        }

        thread {
            logger.info("Trivia Thread detached for ${Core.getDiscordTag(event.author)}")

            users.add(event.author.longID)

            var correctAnswers = 0
            var totalAnswers = 0

            game@ for (trivia in triviaData.results) {
                val answers = trivia.incorrectAnswers.toMutableList().also { it.add(trivia.correctAnswer) }.shuffled()
                buildEmbed(channel) {
                    withAuthorName("Difficulty: ${trivia.difficulty.capitalize()}")
                    withTitle("Category: ${trivia.category}")
                    withDesc(StringEscapeUtils.unescapeHtml4(trivia.question))

                    answers.forEachIndexed { i, answer ->
                        appendField((i + 65).toChar().toString(), StringEscapeUtils.unescapeHtml4(answer), true)
                    }
                }

                var lastMessageId = channel.messageHistory.latestMessage.longID
                logger.debug("Waiting for user input for Question ${totalAnswers + 1} of $questions")
                checkResponse@ while (true) {
                    if (channel.messageHistory.latestMessage.longID != lastMessageId) {
                        val message = channel.messageHistory.latestMessage

                        lastMessageId = message.longID

                        if (message.content.toLowerCase() == "exit") {
                            break@game
                        }

                        if (answers.any { it.toLowerCase() == message.content.toLowerCase() } ||
                                (message.content.length == 1 && (message.content[0].toInt() - 65) < answers.size)) {
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
                        withContent("Monika hit a hiccup and needs to take a break :(")
                    }

                    e.printStackTrace()
                    break@game
                }

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
                                    withContent("You're incorrect... :(\nThe correct answer is ${trivia.correctAnswer}.")
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

            logger.info("Trivia Thread joined for ${Core.getDiscordTag(event.author)}")
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `trivia` (Experimental)")
                withDesc("Starts a trivia game with Monika.")
                insertSeparator()
                appendField("Usage", "```trivia [questions] [difficulty]```", false)
                appendField("`[questions]`", "Number of questions to ask.\nDefaults to 5", false)
                appendField("`[difficulty]`", "Difficulty of the questions. Can be easy, medium, hard, or any.\nDefaults to easy.", false)
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
        }
    }

    fun checkUserTriviaStatus(event: MessageReceivedEvent): Boolean {
        if (users.any { it == event.author.longID }) {
            if (!event.channel.isPrivate) {
                buildMessage(event.channel) {
                    withContent("It looks like you're still in a trivia game... Type \"exit\" in my private chat to quit it!")
                }
            }
            return true
        }

        return false
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