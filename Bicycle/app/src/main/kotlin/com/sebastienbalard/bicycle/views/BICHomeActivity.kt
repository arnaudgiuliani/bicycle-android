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
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.*
import com.sebastienbalard.bicycle.misc.NOTIFICATION_REQUEST_PERMISSION_LOCATION
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.viewmodels.BICMapViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.widget_appbar.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.run

class BICHomeActivity : SBActivity() {

    companion object : SBLog()

    private lateinit var mapViewModel: BICMapViewModel
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<BICStationAnnotation>? = null
    private var contractsAnnotations: MutableList<Marker>? = null

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        v("onCreate")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        mapViewModel = ViewModelProviders.of(this).get(BICMapViewModel::class.java)

        initGoogleMap(savedInstanceState)
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
                            mapViewModel.userLocation.observe(this, Observer { location ->
                                moveCamera(location)
                            })
                            refreshLayout()
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
        mapViewModel.hasCurrentContractChanged.observe(this, Observer { hasChanged ->
            hasChanged?.let {
                if (!it) {
                    v("current contract has not changed")
                    // reload clustering
                    clusterManager?.cluster()
                }
            }
        })
        mapViewModel.currentContract.observe(this, Observer { contract ->
            if (contract == null) {
                d("current bounds is out of contracts covers")
                //stopTimer()
            } else {
                //stopTimer()
                d("refresh contract stations: ${contract.name} (${contract.provider.tag})")
                // refresh current contract stations data
                mapViewModel.loadCurrentContractStations()
                //startTimer()
            }
        })
        mapViewModel.currentStations.observe(this, Observer { stations ->
            clusterManager?.clearItems()
            stations?.map { station ->
                clusterManager?.addItem(BICStationAnnotation(station))
                clusterManager?.cluster()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        v("onPause")
        mapView.onPause()
        mapViewModel.currentContract.removeObservers(this)
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
    private fun refreshMarkers() {
        val level = googleMap?.cameraPosition?.zoom?.toInt()
        level?.let {
            d("current zoom level: $level")
            if (it >= 10) {
                deleteContractsAnnotations()
                mapViewModel.determineCurrentContract(googleMap!!.projection.visibleRegion.latLngBounds)
            } else {
                mapViewModel.currentContract.value = null
                //stopTimer()
                createContractsAnnotations()
            }
        }
    }

    private fun deleteContractsAnnotations() {
        contractsAnnotations?.let {
            if (it.size > 0) {
                d("delete contracts annotations")
                it.map { marker -> marker.remove() }
                it.clear()
            }
        }
    }

    private fun createContractsAnnotations() {
        d("create contracts annotations")
        val hasMarkers = clusterManager?.markerCollection?.markers?.isNotEmpty()?.or(false)!!
        val hasClusterMarkers = clusterManager?.clusterMarkerCollection?.markers?.isNotEmpty()?.or(false)!!
        if (hasMarkers || hasClusterMarkers) {
            clusterManager?.clearItems()
            clusterManager?.cluster()
        }
        if (contractsAnnotations?.size == 0) {
            async(CommonPool) {
                var options: MarkerOptions
                mapViewModel.allContracts.map { contract ->
                    options = MarkerOptions()
                    options.position(contract.center)
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title(contract.name)
                    run(UI) {
                        googleMap?.addMarker(options)?.let {
                            contractsAnnotations?.add(it)
                        }
                    }
                }
                run(UI) {
                    // do this to avoid partial contracts display after activity launch
                    val level = googleMap?.cameraPosition?.zoom?.toInt()
                    level?.let {
                        if (it >= 10) {
                            deleteContractsAnnotations()
                        }
                    }
                }
            }
        }
    }

    private fun moveCamera(location: Location?) {
        location?.let {
            v("move camera to new location")
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
            googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f), 1000, null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshLayout() {
        googleMap?.let {
            val hasLocationPermissions = hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            it.isMyLocationEnabled = hasLocationPermissions
            it.uiSettings.isMyLocationButtonEnabled = hasLocationPermissions
        }
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->

            googleMap = map
            if (googleMap != null) {
                googleMap!!.setOnMapClickListener { latLng -> v("onMapClick") }
                googleMap!!.setOnMarkerClickListener { marker ->
                    v("onMarkerClick")
                    false
                }
                googleMap!!.setOnCameraIdleListener {
                    v("onCameraIdle")
                    refreshMarkers()
                }
                googleMap!!.uiSettings.isCompassEnabled = true
                googleMap!!.uiSettings.isZoomControlsEnabled = true
                googleMap!!.uiSettings.isMapToolbarEnabled = true

                requestLocationPermissionsIfNeeded(NOTIFICATION_REQUEST_PERMISSION_LOCATION, onGranted = {
                    mapViewModel.userLocation.observe(this, Observer { location ->
                        moveCamera(location)
                    })
                    refreshLayout()
                })
                clusterManager = ClusterManager<BICStationAnnotation>(this, googleMap!!)
                clusterManager?.renderer = BICStationAnnotation.Renderer(this, googleMap!!, clusterManager!!)
                googleMap!!.setOnInfoWindowClickListener(clusterManager)
                contractsAnnotations = mutableListOf<Marker>()

            } else {
                Snackbar.make(toolbar, R.string.bic_messages_error_no_play_services_installed, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    //endregion
}
