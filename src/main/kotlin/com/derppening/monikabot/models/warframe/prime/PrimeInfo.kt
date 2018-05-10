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

package com.derppening.monikabot.models.warframe.prime

import java.time.Instant

class PrimeInfo(val name: String, val gender: Char, longDate: Long, longPrimeDate: Long, longPrimeExpiryDate: Long) {
    private val _date: Instant = Instant.ofEpochSecond(longDate)
    val date: Instant?
        get() = if (_date == Instant.EPOCH) {
            null
        } else {
            _date
        }

    private val _primeDate: Instant = Instant.ofEpochSecond(longPrimeDate)
    val primeDate: Instant?
        get() = if (_primeDate == Instant.EPOCH) {
            null
        } else {
            _primeDate
        }

    private val _primeExpiryDate: Instant = Instant.ofEpochSecond(longPrimeExpiryDate)
    val primeExpiry: Instant?
        get() = if (_primeExpiryDate == Instant.EPOCH) {
            null
        } else {
            _primeExpiryDate
        }
}