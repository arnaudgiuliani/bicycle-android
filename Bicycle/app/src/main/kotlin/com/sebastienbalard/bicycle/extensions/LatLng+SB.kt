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

import android.location.Location
import android.location.LocationManager
import com.google.android.gms.maps.model.LatLng

fun LatLng.distanceTo(latLng: LatLng): Float {
    val from = Location(LocationManager.GPS_PROVIDER)
    from.latitude = latitude
    from.longitude = longitude
    val to = Location(LocationManager.GPS_PROVIDER)
    to.latitude = latLng.latitude
    to.longitude = latLng.longitude
    return from.distanceTo(to)
}