package core

import cmds.*
import core.BuilderHelper.buildMessage
import core.Core.popLeadingMention
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import java.io.File

object Parser : IChannelLogger {
    enum class HandleState {
        HANDLED,
        UNHANDLED,
        PERMISSION_DENIED,
        NOT_FOUND
    }

    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        if (!event.channel.isPrivate &&
                !event.message.content.startsWith(Client.ourUser.mention(false))) {
            if (!Core.isOwnerLocationValid(event)) {
                return
            }
        }

        val cmd = getCommand(popLeadingMention(event.message.content)).toLowerCase()

        if (cmd.isBlank()) {
            buildMessage(event.channel) {
                withContent(getRandomNullResponse())
            }
            return
        }

        val runExperimental = event.message.content.split(' ').any { it == "--experimental" }
        val retval: HandleState
        if (runExperimental && Config.enableExperimentalFeatures) {
            retval = parseExperimental(event, cmd)
        } else {
            if (runExperimental && Core.isEventFromSuperuser(event)) {
                buildMessage(event.channel) {
                    withContent("It seems like you're trying to invoke an experimental command without it being on...")
                }
            }

            retval = when (cmd) {
                "changelog" -> Changelog.delegateCommand(event)
                "clear" -> Clear.delegateCommand(event)
                "config" -> Config.delegateCommand(event)
                "debug" -> Debug.delegateCommand(event)
                "echo" -> Echo.delegateCommand(event)
                "help" -> Help.delegateCommand(event)
                "random" -> Random.delegateCommand(event)
                "reload" -> Reload.delegateCommand(event)
                "rng" -> RNG.delegateCommand(event)
                "status" -> Status.delegateCommand(event)
                "stop" -> Stop.delegateCommand(event)
                "version" -> Version.delegateCommand(event)
                "warframe" -> Warframe.delegateCommand(event)
                else -> HandleState.NOT_FOUND
            }
        }

        postCommandHandler(retval, cmd, event)
    }

    fun loadNullResponses(): List<String> {
        nullResponses = File(Thread.currentThread().contextClassLoader.getResource("lang/NullResponse.txt").toURI()).readLines()
        return nullResponses
    }

    private fun parseExperimental(event: MessageReceivedEvent, cmd: String): HandleState {
        return when (cmd) {
            "warframe" -> cmds.experimental.Warframe.delegateCommand(event)
            else -> HandleState.NOT_FOUND
        }
    }

    private fun postCommandHandler(state: HandleState, cmd: String, event: MessageReceivedEvent) {
        when (state) {
            HandleState.HANDLED -> {}
            HandleState.UNHANDLED -> {
                log(IChannelLogger.LogLevel.ERROR, "Command \"$cmd\" not handled") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
            HandleState.NOT_FOUND -> {
                try {
                    buildMessage(event.channel) {
                        withContent("I don't know how to do that! >.<")
                    }
                } catch (e: DiscordException) {}
                log(IChannelLogger.LogLevel.ERROR, "Command \"$cmd\" not found") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
            HandleState.PERMISSION_DENIED -> {
                try {
                    buildMessage(event.channel) {
                        withContent("You're not allow to do this! x(")
                    }
                } catch (e: DiscordException) {}
                log(IChannelLogger.LogLevel.ERROR, "\"$cmd\" was not invoked by superuser") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                }
            }
        }
    }

    private fun getCommand(message: String): String = message.split(' ')[0]

    private fun getRandomNullResponse(): String = nullResponses[java.util.Random().nextInt(nullResponses.size)]

    private var nullResponses = loadNullResponses()
}