import cmds.Config
import cmds.Warframe
import core.Client
import core.Parser

fun setupDispatchers() {
    // core
    Client.dispatcher.registerListener(Client)

    // cmds
    Client.dispatcher.registerListener(Parser)
    Client.dispatcher.registerListener(Config)
}

fun registerTimers() {
    Client.registerTimer(Warframe.updateWorldStateTask)
    Client.registerTimer(cmds.experimental.Warframe.updateWorldStateTask)
}

fun main(args: Array<String>) {
    setupDispatchers()
    registerTimers()
}