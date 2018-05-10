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

import com.derppening.monikabot.core.ILogger

object PingService : ILogger {
    private val digitalOceanPings = mapOf(
            "New Jersey" to "104.236.17.211",
            "Singapore" to "128.199.105.91",
            "Germany" to "165.227.164.4",
            "Netherlands" to "188.166.23.150",
            "California" to "192.184.13.42"
    )

    private val dnsPings = mapOf(
            "Google" to "8.8.8.8",
            "CloudFlare" to "1.1.1.1",
            "Quad9" to "9.9.9.9",
            "OpenDNS" to "208.67.222.222",
            "Yandex DNS" to "77.88.8.7",
            "Comodo DNS" to "8.26.56.26"
    )

    fun getEmbed(): EmbedFields {
        val digitalOceanString = digitalOceanPings.entries.joinToString("\n") { (server, ip) ->
            val p = ProcessBuilder("ping -c 1 $ip".split(" "))
            val process = p.start()
            process.waitFor()

            val str = process.inputStream.bufferedReader().readText()
            val time = Regex("time=((?:[\\d.])+ ms)").find(str)?.groups?.get(1)?.value ?: ""

            if (time.isNotBlank()) {
                "$server: $time"
            } else {
                "$server: Unreachable"
            }
        }

        val dnsString = dnsPings.entries.joinToString("\n") { (server, ip) ->
            val p = ProcessBuilder("ping -c 1 $ip".split(" "))
            val process = p.start()
            process.waitFor()

            val str = process.inputStream.bufferedReader().readText()
            val time = Regex("time=((?:[\\d.])+ ms)").find(str)?.groups?.get(1)?.value ?: ""

            if (time.isNotBlank()) {
                "$server: $time"
            } else {
                "$server: Unreachable"
            }
        }

        return EmbedFields(digitalOceanString, dnsString)
    }

    class EmbedFields(
            val digitalOcean: String,
            val dns: String
    )
}