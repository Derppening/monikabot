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

package com.derppening.monikabot.util.helpers

import java.net.URL
import java.net.URLConnection

private const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"

fun URL.openAndSetUserAgent(userAgent: String = USER_AGENT): URLConnection = openConnection().also {
    it.setRequestProperty("User-Agent", userAgent)
}

fun URLConnection.setUserAgent(userAgent: String = USER_AGENT): URLConnection = also {
    setRequestProperty("User-Agent", userAgent)
}

fun URLConnection.readText(): String = getInputStream().bufferedReader().readText()

fun URLConnection.readLines(): List<String> = getInputStream().bufferedReader().readLines()
