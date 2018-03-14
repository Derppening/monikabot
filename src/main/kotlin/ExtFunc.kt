/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * RTLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RTLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RTLib.  If not, see <http://www.gnu.org/licenses/>.
 */

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
