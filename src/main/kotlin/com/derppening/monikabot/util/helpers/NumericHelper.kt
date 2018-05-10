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

import java.math.BigDecimal
import java.math.MathContext

object NumericHelper {
    enum class Rounding {
        DECIMAL_PLACES,
        SIGNIFICANT_FIGURES
    }

    fun <T> clamp(v: T, lo: T, hi: T, comp: Comparator<T>): T {
        require(comp.compare(lo, hi) < 0)
        return when {
            comp.compare(v, lo) < 0 -> lo
            comp.compare(hi, v) < 0 -> hi
            else -> v
        }
    }

    fun <T> clamp(v: T, lo: T, hi: T, comp: (T, T) -> Boolean): T {
        require(!comp(hi, lo))
        return when {
            comp(v, lo) -> lo
            comp(hi, v) -> hi
            else -> v
        }
    }

    /**
     * Formats a real number.
     *
     * @param double Number to format.
     * @param rounding Type of rounding to use.
     * @param isPercent Whether to format as a percentage.
     *
     * @return String of formatted number.
     */
    fun formatReal(double: Double,
                   rounding: Pair<Rounding, Int> = Rounding.DECIMAL_PLACES to 2,
                   isPercent: Boolean = false): String {
        return when (rounding.first) {
            Rounding.DECIMAL_PLACES -> formatRealDecimal(double, rounding.second, isPercent)
            Rounding.SIGNIFICANT_FIGURES -> formatRealSigFig(double, rounding.second, isPercent)
        }
    }

    /**
     * Formats a real number by decimal places.
     *
     * @param double Number to format.
     * @param dp Decimal places.
     * @param isPercent whether to format as a percentage.
     */
    fun formatRealDecimal(double: Double, dp: Int, isPercent: Boolean): String {
        return if (isPercent) {
            "%.${dp}f%%".format(double * 100)
        } else {
            "%.${dp}f".format(double)
        }
    }

    /**
     * Formats a real number by significant figures.
     *
     * @param double Number to format.
     * @param sf Significant figures.
     * @param isPercent whether to format as a percentage.
     */
    fun formatRealSigFig(double: Double, sf: Int, isPercent: Boolean): String {
        return if (isPercent) {
            "${BigDecimal(double * 100).round(MathContext(sf)).toDouble()}%"
        } else {
            BigDecimal(double).round(MathContext(sf)).toDouble().toString()
        }
    }
}