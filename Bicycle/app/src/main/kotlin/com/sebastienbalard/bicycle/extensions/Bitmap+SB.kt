/**
 * Copyright © 2018 Bicycle (Sébastien BALARD)
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

import android.graphics.*
import android.text.TextPaint


fun Bitmap.drawOn(text: String, color: Int, size: Float, yRate: Float): Bitmap {

    val newBitmap = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(newBitmap)
    val paint = TextPaint()
    paint.color = color
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textSize = size
    paint.style = Paint.Style.FILL
    paint.isAntiAlias = true

    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    val x = (width - bounds.width()).toFloat() / 2 - bounds.left
    val y = ((height + bounds.height()) / yRate)

    canvas.drawText(text, x, y, paint)

    return newBitmap
}
