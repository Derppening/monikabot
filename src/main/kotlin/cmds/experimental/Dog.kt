package cmds.experimental

import cmds.IBase
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.net.URL

object Dog : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).dropWhile { it == "--experimental" }

        when {
            args.isEmpty() -> {
                buildEmbed(event.channel) {
                    withImage(getRandomPic())
                }
            }
            args[0].matches(Regex("-{0,2}list")) -> list(args, event)
            args.isNotEmpty() -> showBreed(args, event)
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `dog`")
            withDesc("Displays a photo of a dog.")
            insertSeparator()
            appendField("Usage", "```dog [breed] [subbreed]```", false)
            appendField("`[breed]", "If specified, only display images of the given breed.", false)
            appendField("`[subbreed]`", "If specified, only display images of the given subbreed. This option must be used in conjunction with `[breed]`.", false)
            insertSeparator()
            appendField("Usage", "```dog --list [breed]```", false)
            appendField("`--list`", "Lists all breeds of dogs that can be retrieved from this command.", false)
            appendField("`[breed]", "If specified, lists all subbreeds of the given breed.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    private fun list(args: List<String>, event: MessageReceivedEvent) {
        val list = getList()

        if (args.size > 1) {
            val breeds = findBreedFuzzy(args.drop(1).joinToString(""))
            when (breeds.size) {
                0 -> {
                    buildMessage(event.channel) {
                        buildMessage(event.channel) {
                            withContent("I can't find a breed named \"${args.drop(1).joinToString("")}!")
                        }
                    }
                }
                1 -> {
                    val breed = args.drop(1).joinToString("").capitalize()
                    val subbreedList = getBreedList(breed)
                    buildEmbed(event.channel) {
                        withTitle("List of Subbreeds of $breed")
                        withDesc(subbreedList.joinToString("\n") { it.capitalize() })

                        withFooterText("Total: ${subbreedList.size}")
                    }
                }
                else -> {
                    buildMessage(event.channel) {
                        withContent("Multiple breeds match your given search! Including:\n\n")
                        appendContent(breeds.take(5).joinToString("\n") { "- ${it.capitalize()}" })
                        if (breeds.size > 5) {
                            appendContent("\n\n...And ${breeds.size - 5} more results.")
                        }
                    }
                }
            }
        } else {
            buildEmbed(event.channel) {
                withTitle("List of Breeds")
                withDesc(list.joinToString("\n") { it.capitalize() })

                withFooterText("Total: ${list.size}")
            }
        }
    }

    private fun showBreed(args: List<String>, event: MessageReceivedEvent) {
        val breeds = findBreedFuzzy(args[0])
        when (breeds.size) {
            0 -> {
                buildMessage(event.channel) {
                    withContent("I can't find a breed named \"${args[0]}\"!")
                }
            }
            1 -> {
                if (args.size == 1) {
                    event.channel.toggleTypingStatus()
                    buildEmbed(event.channel) {
                        withImage(getBreedPic(args[0], ""))
                    }
                } else {
                    showSubbreed(args, event)
                }
            }
            else -> {
                buildMessage(event.channel) {
                    withContent("Multiple breeds match your given search! Including:\n\n")
                    appendContent(breeds.take(5).joinToString("\n") { "- ${it.capitalize()}" })
                    if (breeds.size > 5) {
                        appendContent("\n\n...And ${breeds.size - 5} more results.")
                    }
                }
            }
        }
    }

    private fun showSubbreed(args: List<String>, event: MessageReceivedEvent) {
        val subbreedList = findSubbreedFuzzy(args[0], args[1])
        when (subbreedList.size) {
            0 -> {
                buildMessage(event.channel) {
                    withContent("I can't find a subbreed named \"${args[1]}!")
                }
            }
            1 -> {
                event.channel.toggleTypingStatus()
                buildEmbed(event.channel) {
                    withImage(getBreedPic(args[0], subbreedList.first()))
                }
            }
            else -> {
                buildMessage(event.channel) {
                    withContent("Multiple subbreeds match your given search! Including:\n\n")
                    appendContent(subbreedList.take(5).joinToString("\n") { "- ${it.capitalize()}" })
                    if (subbreedList.size > 5) {
                        appendContent("\n\n...And ${subbreedList.size - 5} more results.")
                    }
                }
            }
        }
    }

    private fun findBreedFuzzy(keyword: String): List<String> {
        val list = getList()

        return when {
            list.any { it.equals(keyword, true) } -> {
                listOf(list.first { it.equals(keyword, true) })
            }
            else -> {
                list.filter { it.startsWith(keyword, true) }
            }
        }
    }

    private fun findSubbreedFuzzy(breed: String, keyword: String): List<String> {
        val list = getBreedList(breed)

        return when {
            list.any { it.equals(keyword, true) } -> {
                listOf(list.first { it.equals(keyword, true) })
            }
            else -> {
                list.filter { it.startsWith(keyword, true) }
            }
        }
    }

    private fun getRandomPic(): String {
        return jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.readTree(URL("https://dog.ceo/api/breeds/image/random")).get("message").asText()
    }

    private fun getBreedPic(breed: String, subbreed: String): String {
        return jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.let {
            if (subbreed.isBlank()) {
                it.readTree(URL("https://dog.ceo/api/breed/$breed/images/random"))
            } else {
                it.readTree(URL("https://dog.ceo/api/breed/$breed/$subbreed/images/random"))
            }
        }.get("message").asText()
    }

    private fun getList(): List<String> {
        return jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.readTree(URL("https://dog.ceo/api/breeds/list"))
                .get("message")
                .let {
                    ObjectMapper().apply {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    }.readValue(it.toString())
                }
    }

    private fun getBreedList(subbreed: String): List<String> {
        return jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        }.readTree(URL("https://dog.ceo/api/breed/$subbreed/list"))
                .get("message")
                .let {
                    ObjectMapper().apply {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    }.readValue(it.toString())
                }
    }
}