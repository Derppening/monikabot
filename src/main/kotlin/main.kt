import cmds.Echo
import core.Client
import core.Shard

fun setupDispatchers() {
    // core
    Client.dispatcher.registerListener(Client)
    Client.dispatcher.registerListener(Shard())

    Client.dispatcher.registerListener(Echo())
}

fun main(args: Array<String>) {
    setupDispatchers()
}