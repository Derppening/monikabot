/*
 *  This file is part of MonikaBot.
 *
 *  Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 *  MonikaBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MonikaBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.derppening.monikabot.impl

import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.util.helpers.openAndSetUserAgent
import com.derppening.monikabot.util.helpers.readLines
import org.apache.commons.text.StringEscapeUtils
import java.net.URL

object ToiletService : ILogger {
    private val emojiTextMap = mapOf(
            '?' to "question",
            '!' to "exclamation"
    )

    fun String.toASCIIText(font: String? = null): String {
        val strText = StringEscapeUtils.escapeHtml4(this).replace(' ', '+')
        val url = if (font != null) {
            "http://artii.herokuapp.com/make?text=$strText&font=$font"
        } else {
            "http://artii.herokuapp.com/make?text=$strText"
        }

        val fontLog = if (font == null) { font.toString() } else { "\"$font\"" }
        logger.debugFun(Core.getMethodName("font = $fontLog")) { "Fetching from $url" }
        return URL(url).openAndSetUserAgent().readLines().let {
            it.map { it.trimEnd() }
        }.joinToString("\n")
    }

    /**
     * Converts into text represented by emoji syntax in discord.
     *
     * @param delimiter Delimiter for each word.
     */
    fun String.toEmojiText(delimiter: String = ":small_blue_diamond:"): String {
        return map {
            when {
                it.isLetter() -> ":regional_indicator_${it.toLowerCase()}:"
                it in emojiTextMap -> ":${emojiTextMap[it]}:"
                else -> it.toString()
            }
        }.joinToString("")
                .replace(" ", delimiter)
                .replace("::", ": :")
                .trim()
    }
}