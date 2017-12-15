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

package com.sebastienbalard.bicycle.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.annotations.SerializedName
import com.google.maps.android.SphericalUtil
import java.lang.Math.sqrt

data class BICContract(val name: String, @SerializedName("lat") val latitude: Double, @SerializedName("lng") val longitude: Double, val provider: Provider, val radius: Double, val url: String) {

    val center: LatLng
        get() = LatLng(latitude, longitude)

    val bounds: LatLngBounds
        get() {
            val distanceFromCenterToCorner = radius * sqrt(2.0)
            val southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
            val northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
            return LatLngBounds(southwestCorner, northeastCorner)
        }

    enum class Provider(val value: Int, val tag: String) {
        Unknown(0, "Unknown"),
        CityBikes(1, "CityBikes");

        companion object {
            fun from(tag: String): Provider = Provider.values().first { it.tag == tag }
        }
    }
}