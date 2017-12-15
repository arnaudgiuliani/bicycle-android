/**
 * Copyright © 2017 Bicycle (Sébastien BALARD)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastienbalard.bicycle.extensions

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

fun Double.format(fractionDigits: Int = 0, unitSymbol: String? = null): String {

    val formatter = DecimalFormat()
    formatter.minimumIntegerDigits = 1
    formatter.maximumFractionDigits = fractionDigits
    formatter.minimumFractionDigits = fractionDigits
    formatter.multiplier = 1
    formatter.roundingMode = RoundingMode.HALF_UP

    var label = formatter.format(this)
    unitSymbol?.let { label += " $unitSymbol" }
    return label
}

fun Float.format(fractionDigits: Int = 0, unitSymbol: String = ""): String {
    return this.toDouble().format(fractionDigits, unitSymbol)
}