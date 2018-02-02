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

package com.sebastienbalard.bicycle.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import com.sebastienbalard.bicycle.R

typealias OnClearListener = (() -> Unit)

class SBClearableAutoCompleteView(context: Context, attrs: AttributeSet?) :
        AppCompatAutoCompleteTextView(context, attrs), TextWatcher {

    private var buttonClear: Drawable = ContextCompat.getDrawable(context, R.drawable.bic_ic_clear_24dp)!!

    private var listener: OnClearListener? = null

    init {
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !text.toString().isEmpty()) {
                setCompoundDrawables(null, null, buttonClear, null)
            } else {
                setCompoundDrawables(null, null, null, null)
            }
        }
        setOnTouchListener({ _, event ->
            if (this.compoundDrawables[2] == null) {
                false
            }
            if (event.action != MotionEvent.ACTION_UP) {
                false
            }
            if (event.x > (width - paddingRight - buttonClear.intrinsicWidth)) {
                setText("")
                setCompoundDrawables(null, null, null, null)
                listener?.invoke()
            }
            false
        })
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isNotEmpty()) {
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, buttonClear, null)
        } else {
            this.setCompoundDrawables(null, null, null, null)
        }
    }

    override fun afterTextChanged(s: Editable) {}

    fun setOnClearListener(l: OnClearListener) {
        listener = l
    }
}
