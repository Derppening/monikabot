import cmds.*

fun setupDispatchers() {
    Client.Instance.dispatcher.registerListener(Client())
    Client.Instance.dispatcher.registerListener(Echo())
}

fun main(args: Array<String>) {
    setupDispatchers()
}