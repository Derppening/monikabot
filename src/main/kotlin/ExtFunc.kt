/**
 * Pops the first word in a string.
 */
fun String.popFirstWord(): String = dropWhile { it != ' ' }.dropWhile { it == ' ' }

/**
 * Remove quotes from a word.
 */
fun String.removeQuotes(): String = dropWhile { it == '\"' }.dropLastWhile { it == '\"' }
