import cmds.Echo

fun setupDispatchers() {
    Client.dispatcher.registerListener(Client)
    Client.dispatcher.registerListener(Echo())
}

fun main(args: Array<String>) {
    setupDispatchers()
}