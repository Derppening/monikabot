fun String.popFirstWord(): String = dropWhile { it != ' ' }.dropWhile { it == ' ' }

fun String.removeQuotes(): String = dropWhile { it == '\"' }.dropLastWhile { it == '\"' }
