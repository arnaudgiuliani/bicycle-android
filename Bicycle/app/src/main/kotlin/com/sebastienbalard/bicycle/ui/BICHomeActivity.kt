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

package com.sebastienbalard.bicycle.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.getIntentForApplicationSettings
import com.sebastienbalard.bicycle.extensions.hasPermissions
import com.sebastienbalard.bicycle.misc.NOTIFICATION_REQUEST_PERMISSION_LOCATION
import com.sebastienbalard.bicycle.misc.SBLog
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.widget_appbar.*


class BICHomeActivity : SBActivity() {

    companion object: SBLog()

    private val locationListener = { location: Location ->
        v("receive user location update")
        userLocation = location
        refreshLayout()
    }

    var userLocation: Location? = null
    var googleApiClient: GoogleApiClient? = null
    var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        v("onCreate")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        mapView.onCreate(savedInstanceState)
        initGoogleMap()
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        v("onStart")
        mapView.onStart()

        googleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).build()
        googleApiClient?.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {

            override fun onConnected(pBundle: Bundle?) {
                d("google api client is connected")
                if (hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest
                        .permission.ACCESS_FINE_LOCATION)) {
                    startLocationUpdates()
                }
            }

            override fun onConnectionSuspended(pCause: Int) {
                w("google api client connection suspended, trying to reconnect...")
            }
        })
        googleApiClient?.registerConnectionFailedListener({ e("google api client connection failed") })
        googleApiClient?.connect()

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest
                    .permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest
                        .permission.ACCESS_FINE_LOCATION), NOTIFICATION_REQUEST_PERMISSION_LOCATION)
            }
        }*/
        /*requestPermissions(NOTIFICATION_REQUEST_PERMISSION_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest
                .permission.ACCESS_FINE_LOCATION)*/
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_REQUEST_PERMISSION_LOCATION ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do nothing
                } else {
                    Snackbar.make(toolbar, R.string.bic_messages_warning_request_location_permissions, Snackbar.LENGTH_LONG).
                            setAction(R.string.bic_actions_allow, {
                                startActivityForResult(getIntentForApplicationSettings(), NOTIFICATION_REQUEST_PERMISSION_LOCATION)
                            }).show()
                }
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        v("onResume")
        mapView.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        v("onPause")
        mapView.onPause()
        googleApiClient?.isConnected.let {
            v("stop location updates")
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)
        }
    }

    override fun onStop() {
        super.onStop()
        v("onStop")
        mapView.onStop()
        googleApiClient?.isConnected.let {
            v("disconnect")
            googleApiClient?.disconnect()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        v("onLowMemory")
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        v("onDestroy")
        mapView.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            v("start location updates")

            val locationRequest = LocationRequest()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.smallestDisplacement = 50f

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener)
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshLayout() {
        googleMap.let {
            val hasLocationPermissions = hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            googleMap!!.isMyLocationEnabled = hasLocationPermissions
            googleMap!!.uiSettings.isMyLocationButtonEnabled = hasLocationPermissions
        }
    }

    private fun initGoogleMap() {
        mapView.getMapAsync { map ->

            googleMap = map
            if (googleMap != null) {
                googleMap!!.setOnMapClickListener { latLng -> v("onMapClick") }
                googleMap!!.setOnMarkerClickListener { marker ->
                    v("onMarkerClick")
                    false
                }
                googleMap!!.uiSettings.isCompassEnabled = true
                googleMap!!.uiSettings.isZoomControlsEnabled = true
                googleMap!!.uiSettings.isMapToolbarEnabled = true

                //OLSGoogleMapsUtils.alignTopZoomControls(this, mMapView)

            } else {
                Snackbar.make(toolbar, R.string.bic_messages_error_no_play_services_installed, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
