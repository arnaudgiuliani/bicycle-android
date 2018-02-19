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

package com.sebastienbalard.bicycle.models

import android.location.Location
import com.google.android.gms.maps.model.LatLng

data class BICPlace(private val latitude: Double, private val longitude: Double) {

    var contract: BICContract? = null

    constructor(latLng: LatLng) : this(latLng.latitude, latLng.longitude)
    constructor(location: Location) : this(location.latitude, location.longitude)

    val location: LatLng
        get() = LatLng(latitude, longitude)
}