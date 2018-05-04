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

import java.io.File

object ChangelogService {
    fun getAll(showRel: Boolean, entries: Int = 5): List<Pair<String, List<String>>> {
        return if (showRel) {
            changes.filterNot { (k, _) -> k.contains('-') }
        } else {
            changes
        }.takeLast(entries)
    }

    fun getLatest(showRel: Boolean): Pair<String, List<String>>? {
        return try {
            if (showRel) {
                changes.filterNot { (k, _) -> k.contains('-') }
            } else {
                changes
            }.last()
        } catch (nsee: NoSuchElementException) {
            null
        }
    }

    private val changes = run {
        val contents = File(Thread.currentThread().contextClassLoader.getResource("lang/Changelog.md").toURI()).readLines()

        val logMap = mutableMapOf<String, MutableList<String>>()
        var ver = ""
        for (line in contents) {
            if (line.startsWith('[') && line.endsWith(']')) {
                ver = line.substring(1, line.lastIndex)
                logMap[ver] = mutableListOf()
            } else if (ver.isNotBlank()) {
                logMap[ver]?.add(line)
            }
        }

        logMap.map { it.key to it.value.toList() }
    }

}