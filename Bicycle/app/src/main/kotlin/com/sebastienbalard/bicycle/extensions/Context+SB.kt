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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.TypedValue
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun Context.getIntentForApplicationSettings(): Intent {
    return Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + packageName)).
            addCategory(Intent.CATEGORY_DEFAULT).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

fun Context.getBitmapDescriptor(id: Int, width: Int, height: Int): BitmapDescriptor? {

    return if (Build.VERSION.SDK_INT >= 21) {
        ContextCompat.getDrawable(this, id)?.let {
            it.setBounds(0, 0, width, height)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            it.draw(canvas)

            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    } else {
        BitmapDescriptorFactory.fromResource(id)
    }
}

fun Context.getBitmap(id: Int, width: Int, height: Int): Bitmap {

    var drawable = ContextCompat.getDrawable(this, id)
    /*if (Build.VERSION.SDK_INT < 21) {
        drawable = DrawableCompat.wrap(drawable!!).mutate()
    }*/

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable?.setBounds(0, 0, canvas.width, canvas.height)
    drawable?.draw(canvas)

    return bitmap
}

fun Context.dpToPx(dp: Int): Int {
    return Math.round(dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun Context.spToPx(sp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics)
}
