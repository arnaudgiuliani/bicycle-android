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

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.sebastienbalard.bicycle.misc.SBLog

class SBLocationLiveData(context: Context) : MutableLiveData<Location>(), LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    companion object : SBLog()

    private val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(context)
            .addConnectionCallbacks(this)
            .addApi(LocationServices.API)
            .build()

    private val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 50f
        interval = 60000
        setExpirationDuration(300000)
    }

    override fun onActive() {
        super.onActive()
        googleApiClient.connect()
    }

    override fun onInactive() {
        super.onInactive()
        if (googleApiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
            googleApiClient.disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) {
        d("google api client is connected")
        LocationServices.FusedLocationApi.getLastLocation(googleApiClient)?.let {
            value = it
        }
        if (hasObservers()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
        }
    }

    override fun onConnectionSuspended(cause: Int) {
        w("google api client connection suspended, trying to reconnect...")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        e("google api client connection failed")
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            d("receive location update: (${it.latitude},${it.longitude})")
            value = it
        }
    }
}