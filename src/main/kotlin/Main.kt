import cmds.Warframe
import core.Client

fun setupDispatchers() {
    // core
    Client.dispatcher.registerListener(Client)

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