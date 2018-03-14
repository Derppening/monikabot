import sx.blah.discord.util.EmbedBuilder

/**
 * Pops the first word in a string.
 */
fun String.popFirstWord(): String = dropWhile { it != ' ' }.dropWhile { it == ' ' }

/**
 * Remove quotes from a word.
 */
fun String.removeQuotes(): String = dropWhile { it == '\"' }.dropLastWhile { it == '\"' }

/**
 * Inserts an empty key-value field as a separator.
 */
fun EmbedBuilder.insertSeparator(): EmbedBuilder = this.appendField("\u200B", "\u200B", false)
