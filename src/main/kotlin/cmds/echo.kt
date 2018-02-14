package cmds

import core.Client
import core.Log
import getBotAdmin
import popFirstWord
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MessageBuilder
import kotlin.system.exitProcess

class Echo {
    @EventSubscriber
    fun onReceiveMessage(event: MessageReceivedEvent) {
        if (!event.channel.isPrivate) return

        if (event.channel.isPrivate && event.author == event.client.fetchUser(getBotAdmin())) {
            adminMessage(event)
        }

        if (!event.message.content.startsWith(event.client.ourUser.mention(false))) return

        val channel = event.channel

        val message = event.message.content.popFirstWord()

        try {
            MessageBuilder(event.client).apply {
                withChannel(channel)
                withContent(message)
            }.build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }

    private fun adminChangeStatus(event: MessageReceivedEvent) {
        val list = event.message.content.split(' ').drop(0).toMutableList()

        val status = when (list[0]) {
            "--reset" -> {
                list.clear()
                list.add(0, Client.defaultStatus)
                Client.defaultState
            }
            "--idle" -> {
                list.removeAt(0)
                Client.Status.IDLE
            }
            "--dnd", "--busy" -> {
                list.removeAt(0)
                Client.Status.BUSY
            }
            "--offline", "--invisible" -> {
                list.removeAt(0)
                Client.Status.OFFLINE
            }
            else -> Client.Status.ONLINE
        }

        val message = list.joinToString(" ")

        try {
            Client.setStatus(status, message)

            MessageBuilder(event.client).apply {
                withChannel(event.channel)
                withContent("Status is Set!")
            }.build()
        } catch (ex: DiscordException) {
            ex.printStackTrace()

            try {
                MessageBuilder(event.client).apply {
                    withChannel(event.channel)
                    withContent("I can't set the status... =/")
                }.build()
            } catch (ex: Exception) {
                // not handled. the stack trace is enough
            }
        }
    }

    private fun adminMessage(event: MessageReceivedEvent) {
        when (event.message.content.takeWhile { it != ' ' }) {
            "kill" -> {
                val message = event.message.content.popFirstWord()

                MessageBuilder(event.client).apply {
                    withChannel(event.channel)
                    withCode("py", "print(\"$message\")")
                }.build()
            }
            "status" -> adminChangeStatus(event)
            "stop" -> {
                event.client.shards.forEach {
                    Log.minus("Logging out shard[${it.info[0]}] (Total: ${it.info[1]})")
                    it.logout()
                }
                exitProcess(0)
            }
            else -> {
                println("Event \"${event.message.content}\" not handled")
            }
        }
    }
}