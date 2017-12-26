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
import android.support.v4.content.ContextCompat
import android.widget.TextView
import android.widget.Toast
import com.sebastienbalard.bicycle.R

fun Toast.showAsError(into: Context) {
    view.setBackgroundColor(ContextCompat.getColor(into, R.color.bic_color_red))
    val textView = view.findViewById(android.R.id.message) as TextView
    textView.setTextColor(ContextCompat.getColor(into, R.color.bic_color_white))
    show()
}