package cmds

import getBotAdmin
import sx.blah.discord.api.events.Event
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

        if (!event.message.content.startsWith(Client.Instance.client.ourUser.mention(false))) return

        val channel = event.channel

        val list = event.message.content.split(' ').toMutableList()
        list.removeAt(0)
        val message = list.joinToString(" ")

        try {
            MessageBuilder(Client.Instance.client)
                    .withChannel(channel)
                    .withContent(message)
                    .build()
        } catch (e: DiscordException) {
            e.printStackTrace()
        }
    }

    private fun adminMessage(event: MessageReceivedEvent) {
        when (event.message.content.split(' ')[0]) {
            "status" -> {
                val list = event.message.content.split(' ').toMutableList()
                list.removeAt(0)

                val status = when (list[0]) {
                    "--reset" -> {
                        list.clear()
                        list.add(0, Client.Instance.defaultStatus)
                        Client.Instance.defaultState
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
                    Client.Instance.setStatus(status, message)

                    MessageBuilder(Client.Instance.client)
                            .withChannel(event.channel)
                            .withContent("Status is Set!")
                            .build()
                } catch (ex: DiscordException) {
                    ex.printStackTrace()

                    try {
                        MessageBuilder(Client.Instance.client)
                                .withChannel(event.channel)
                                .withContent("I can't set the status... =/")
                                .build()
                    } catch (ex: Exception) {
                        // not handled. the stack trace is enough
                    }
                }
            }
            "stop" -> exitProcess(0)
            else -> {
                println("Event not handled")
            }
        }
    }
}