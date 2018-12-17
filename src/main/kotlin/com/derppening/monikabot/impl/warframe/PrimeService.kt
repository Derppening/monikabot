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

package com.derppening.monikabot.impl.warframe

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.models.warframe.prime.PrimeInfo
import com.derppening.monikabot.util.helpers.toNearestChronoYear
import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

object PrimeService : ILogger {
    private val primesFile = Paths.get("resources/primes.csv").toUri()
    private val PRIMES_FILE_HEADER = listOf(
        "warframe",
        "gender",
        "originalRelease",
        "primeRelease",
        "primeVaulted"
    )

    private val allInfo
        get() = readFromFile().filterNot { it.name == "Excalibur" }
    private val primes = allInfo.filter { it.primeDate != null }.sortedBy { it.primeDate?.epochSecond ?: 0 }
    private val nonprimes = allInfo.filter { it.primeDate == null }.sortedBy { it.date?.epochSecond ?: 0 }

    fun getReleasedPrimesStr(size: Int): List<String> {
        val released = primes.takeLast(size)

        return released.mapIndexed { i, it ->
            val duration = when {
                Duration.between(it.primeDate, Instant.now()).isNegative -> {
                    Duration.between(it.primeDate, Instant.now())
                }
                i == released.lastIndex || Duration.between(released[i + 1].primeDate, Instant.now()).isNegative -> {
                    Duration.ZERO
                }
                else -> Duration.between(it.primeDate, released[i + 1].primeDate)
            }
            val durationText = when {
                duration.isNegative -> "(Releasing in ${duration.abs().toDays()} days)"
                duration == Duration.ZERO -> ""
                else -> "(Lasted for ${duration.toDays()} days)"
            }

            "\n\t- ${it.name} $durationText".trim(' ')
        }
    }

    private fun getReleasedPrimes(size: Int): List<PrimeInfo> {
        return primes.takeLast(size)
    }

    fun getPredictedPrimesStr(size: Int): List<String> {
        val averageDuration = primes.zipWithNext().let { pairs ->
            pairs.sumBy {
                Duration.between(it.first.primeDate, it.second.primeDate).toDays().toInt()
            }.div(pairs.size)
        }

        var time = getReleasedPrimes(size).last().primeDate ?: error("Primes should have a prime date.")
        val male = getPredictedPrimes(size).filter { it.gender.toUpperCase() == 'M' }.sortedBy {
            it.date?.epochSecond ?: 0
        }.toMutableList()
        val female = getPredictedPrimes(size).filter { it.gender.toUpperCase() == 'F' }.sortedBy {
            it.date?.epochSecond ?: 0
        }.toMutableList()

        val currentPrimes = primes.subList(primes.size - 2, primes.size).toMutableList()
        val predictedStr = mutableListOf<String>()
        while (male.isNotEmpty() || female.isNotEmpty()) {
            time = time.plus(averageDuration.toLong(), ChronoUnit.DAYS)

            val gender = currentPrimes[currentPrimes.size - 2].gender.toUpperCase()
            when {
                gender == 'M' && female.isNotEmpty() || gender == 'F' && male.isEmpty() -> {
                    currentPrimes.add(female[0])
                    female.removeAt(0)
                }
                else -> {
                    currentPrimes.add(male[0])
                    male.removeAt(0)
                }
            }
            val durationToPrime = Duration.between(Instant.now(), time)
            val durationStr = durationToPrime.toNearestChronoYear()
            predictedStr.add("\n\t- ${currentPrimes.last().name} (In ~$durationStr)")
        }

        return predictedStr.toList()
    }

    private fun getPredictedPrimes(size: Int): List<PrimeInfo> {
        return nonprimes.take(size)
    }

    private fun readFromFile(): List<PrimeInfo> {
        val lines = File(primesFile)
            .also {
                check(it.exists())
            }.readLines()
            .also {
                val fieldHeaders = it.first().split(',')
                check(fieldHeaders.size == 5)

                fieldHeaders.forEachIndexed { i, s -> check(s == PRIMES_FILE_HEADER[i]) }
            }
            .drop(1)

        return lines.map {
            val props = it.split(',')
            check(props.size == 5)
            PrimeInfo(
                props[0],
                props[1][0],
                props[2].toLongOrNull() ?: 0L,
                props[3].toLongOrNull() ?: 0,
                props[4].toLongOrNull() ?: 0
            )
        }
    }
}