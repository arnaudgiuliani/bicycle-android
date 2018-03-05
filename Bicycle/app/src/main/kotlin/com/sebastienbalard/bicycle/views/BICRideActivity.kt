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

package com.sebastienbalard.bicycle.views

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.getBitmap
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICPlace
import com.sebastienbalard.bicycle.viewmodels.BICHomeViewModel
import com.sebastienbalard.bicycle.viewmodels.BICRideViewModel
import kotlinx.android.synthetic.main.bic_widget_appbar.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.koin.android.architecture.ext.viewModel

class BICRideActivity : SBMapActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICRideActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }

        fun getIntentForRide(context: Context, departureLatitude: Double, departureLongitude: Double,
                             arrivalLatitude: Double, arrivalLongitude: Double, bikesCount: Int, freeSlotsCount: Int): Intent {
            return Intent(context, BICRideActivity::class.java).putExtra("departure_latitude", departureLatitude)
                    .putExtra("departure_longitude", departureLongitude)
                    .putExtra("arrival_latitude", arrivalLatitude)
                    .putExtra("arrival_longitude", arrivalLongitude)
                    .putExtra("bikes_count", bikesCount)
                    .putExtra("free_slots_count", freeSlotsCount)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    private val viewModelRide: BICRideViewModel by viewModel()

    private var listDepartureNearestStationsAnnotations: MutableList<Marker>? = null
    private var listArrivalNearestStationsAnnotations: MutableList<Marker>? = null

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_ride)
        v("onCreate")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModelRide.departure = BICPlace(intent.extras.getDouble("departure_latitude"),
                intent.extras.getDouble("departure_longitude"))
        viewModelRide.arrival = BICPlace(intent.extras.getDouble("arrival_latitude"),
                intent.extras.getDouble("arrival_longitude"))

        listDepartureNearestStationsAnnotations = mutableListOf()
        listArrivalNearestStationsAnnotations = mutableListOf()

        initMap(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        v("onResume")
        viewModelRide.departureNearestStations.observe(this, Observer { stations ->
            async(CommonPool) {
                var options: MarkerOptions
                stations?.map { station ->
                    options = MarkerOptions()
                    options.position(station.location)
                    options.icon(BitmapDescriptorFactory.fromBitmap(station.icon))
                    options.title(station.name)
                    kotlinx.coroutines.experimental.run(UI) {
                        googleMap?.addMarker(options)?.let {
                            listDepartureNearestStationsAnnotations?.add(it)
                        }
                    }
                }
            }
        })
        viewModelRide.arrivalNearestStations.observe(this, Observer { stations ->
            async(CommonPool) {
                var options: MarkerOptions
                stations?.map { station ->
                    options = MarkerOptions()
                    options.position(station.location)
                    options.icon(BitmapDescriptorFactory.fromBitmap(station.icon))
                    options.title(station.name)
                    kotlinx.coroutines.experimental.run(UI) {
                        googleMap?.addMarker(options)?.let {
                            listArrivalNearestStationsAnnotations?.add(it)
                        }
                    }
                }
            }
        })
    }

    //endregion

    //region Map events

    override fun onMapInitialized() {

        val size = resources.getDimensionPixelSize(R.dimen.bic_size_annotation_place)
        val imagePlace = getBitmap(R.drawable.bic_img_flag, size, size, R.color.bic_color_orange)

        viewModelRide.determineNearestStations()

        var options = MarkerOptions()
        options.position(viewModelRide.departure.location)
        options.icon(BitmapDescriptorFactory.fromBitmap(imagePlace))
        options.title(getString(R.string.bic_commons_departure))
        options.anchor(0.25f, 1f)
        val departureMarker = googleMap?.addMarker(options)
        options = MarkerOptions()
        options.position(viewModelRide.arrival.location)
        options.icon(BitmapDescriptorFactory.fromBitmap(imagePlace))
        options.title(getString(R.string.bic_commons_arrival))
        options.anchor(0.25f, 1f)
        val arrivalMarker = googleMap?.addMarker(options)

        val vBoundsBuilder = LatLngBounds.Builder()
        vBoundsBuilder.include(departureMarker?.position)
        vBoundsBuilder.include(arrivalMarker?.position)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(vBoundsBuilder
                .build(), 200))
    }

    override fun onMapRefreshed(hasLocationPermissions: Boolean) {

    }

    override fun onUserLocationChanged(location: Location?) {

    }

    override fun onMarkerClicked(marker: Marker) {

    }

    override fun onCameraIdle() {

    }

    //endregion

}
