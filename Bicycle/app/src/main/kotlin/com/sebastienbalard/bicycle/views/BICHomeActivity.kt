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

package com.sebastienbalard.bicycle.views

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.getIntentForApplicationSettings
import com.sebastienbalard.bicycle.extensions.hasPermissions
import com.sebastienbalard.bicycle.extensions.processPermissionsResults
import com.sebastienbalard.bicycle.extensions.requestLocationPermissionsIfNeeded
import com.sebastienbalard.bicycle.misc.NOTIFICATION_REQUEST_PERMISSION_LOCATION
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.viewmodels.BICMapViewModel
import com.sebastienbalard.bicycle.models.SBLocationLiveData
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.widget_appbar.*

class BICHomeActivity : SBActivity() {

    companion object : SBLog()

    private lateinit var mapViewModel: BICMapViewModel
    private var googleMap: GoogleMap? = null

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        v("onCreate")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        mapViewModel = ViewModelProviders.of(this).get(BICMapViewModel::class.java)
        mapViewModel.userLocation = SBLocationLiveData(this)

        mapView.onCreate(savedInstanceState)
        initGoogleMap()
    }

    override fun onStart() {
        super.onStart()
        v("onStart")
        mapView.onStart()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_REQUEST_PERMISSION_LOCATION ->
                processPermissionsResults(permissions, grantResults,
                        onGranted = {
                            mapViewModel.observeLocationUpdates(this, Observer { location ->
                                location?.let {
                                    v("move camera to new location")
                                    googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                                    googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f), 1000, null)
                                }
                            })
                            this.refreshLayout()
                        },
                        onDenied = {
                            Snackbar.make(toolbar, R.string.bic_messages_warning_request_location_permissions, Snackbar.LENGTH_LONG).
                                    setAction(R.string.bic_actions_allow, {
                                        startActivityForResult(getIntentForApplicationSettings(), NOTIFICATION_REQUEST_PERMISSION_LOCATION)
                                    }).show()
                        })
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        v("onResume")
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        v("onPause")
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        v("onStop")
        mapView.onStop()
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
    //endregion

    //region Private methods
    @SuppressLint("MissingPermission")
    private fun refreshLayout() {
        googleMap?.let {
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

                requestLocationPermissionsIfNeeded(NOTIFICATION_REQUEST_PERMISSION_LOCATION, onGranted = {
                    mapViewModel.observeLocationUpdates(this, Observer { location ->
                        location?.let {
                            v("move camera to new location")
                            googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
                            googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f), 1000, null)
                        }
                    })
                    this.refreshLayout()
                })

            } else {
                Snackbar.make(toolbar, R.string.bic_messages_error_no_play_services_installed, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    //endregion
}
