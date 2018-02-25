import cmds.Warframe
import core.Client
import core.Shard

fun setupDispatchers() {
    // core
    Client.dispatcher.registerListener(Client)
    Client.dispatcher.registerListener(Shard)

    // cmds
    Client.dispatcher.registerListener(Parser)
}

fun registerTimers() {
    Client.registerTimer(Warframe.updateWorldStateTask)
}

fun main(args: Array<String>) {
    setupDispatchers()
    registerTimers()
}