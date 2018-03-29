/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package core

import core.Persistence.debugChannel
import sx.blah.discord.handle.obj.IChannel
import java.time.Instant

object PersistentMessage : IChannel by debugChannel, ILogger {
    /**
     * Modifies persistent buildMessage.
     *
     * @param header Header which the key belongs to.
     * @param key Description of the key.
     * @param value Value of the key.
     * @param doUpdate Whether to commit the updated message instantly.
     */
    fun modify(header: String, key: String, value: String, doUpdate: Boolean = false) {
        if (value.isBlank()) {
            map[header]?.remove(key)
        } else {
            if (map[header] == null) {
                map[header] = mutableMapOf(Pair(key, value)).toSortedMap()
            } else {
                map[header]?.put(key, value)
            }
        }

        map.filterValues { it.isEmpty() }.forEach { (k, _) -> map.remove(k) }

        if (doUpdate) {
            update()
        }
    }

    /**
     * Updates the persistent message in the debug channel.
     */
    private fun update() {
        logger.warn("update() is deprecated")

        PersistentMessage.modify("Misc", "Last Updated", Instant.now().toString())

        val s = if (map.isNotEmpty()) {
            map.entries
                    .sortedWith(compareBy({ it.key == "Misc" }, { it.key }))
                    .joinToString("\n\n") { (h, p) ->
                        val pairsInHeader = p.entries.joinToString("\n") { (k, v) ->
                            "$k: $v"
                        }
                        "[$h]\n$pairsInHeader"
                    }
        } else {
            "Nothing to see here!"
        }
    }

    /**
     * Map of persistent information.
     */
    private val map = mutableMapOf<String, MutableMap<String, String>>()
}
