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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.view.inputmethod.InputMethodManager


fun Activity.requestLocationPermissionsIfNeeded(requestCode: Int, onGranted: () -> Unit) {
    requestPermissionsIfNeeded(requestCode,
            onGranted,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
}

fun Activity.requestPermissionsIfNeeded(requestCode: Int, onGranted: () -> Unit, vararg permissions: String) {
    val toRequest = permissions
            .filterNot { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
            .toTypedArray()
    if (toRequest.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, toRequest, requestCode)
    } else {
        onGranted()
    }
}

fun Activity.processPermissionsResults(permissions: Array<out String>,
                                             grantResults: IntArray,
                                             onGranted: () -> Unit,
                                             onDenied: (Boolean) -> Unit) {
    if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
        onGranted()
    } else {
        val showHint = permissions.any { !ActivityCompat.shouldShowRequestPermissionRationale(this, it) }
        onDenied(showHint)
    }
}

fun Activity.hasPermissions(vararg permissions: String): Boolean {
    val toRequest = permissions
            .filterNot { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
            .toTypedArray()
    return toRequest.isEmpty()
}

fun Activity.showSoftInput() {
    val vImm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    vImm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun Activity.hideSoftInput() {
    val vImm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (vImm.isActive) {
        vImm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}
