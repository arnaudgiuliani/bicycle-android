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

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.getBitmap
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.viewmodels.BICHomeViewModel
import kotlinx.android.synthetic.main.bic_widget_appbar.*
import org.koin.android.architecture.ext.viewModel

class BICRideActivity : SBMapActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICRideActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }

        fun getIntentForRide(context: Context, departureLatitude: Double, departureLongitude: Double,
                             arrivalLatitude: Double, arrivalLongitude: Double): Intent {
            return Intent(context, BICRideActivity::class.java).putExtra("departure_latitude", departureLatitude)
                    .putExtra("departure_longitude", departureLongitude)
                    .putExtra("arrival_latitude", arrivalLatitude)
                    .putExtra("arrival_longitude", arrivalLongitude)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    private val viewModelRide: BICHomeViewModel by viewModel()

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_ride)
        v("onCreate")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initMap(savedInstanceState)
    }

    //endregion

    override fun onMapInitialized() {

        val size = resources.getDimensionPixelSize(R.dimen.bic_size_annotation_place)
        val imagePlace = getBitmap(R.drawable.bic_img_flag, size, size, R.color.bic_color_orange)

        val departureLocation = LatLng(intent.extras.getDouble("departure_latitude"),
                intent.extras.getDouble("departure_longitude"))
        val arrivalLocation = LatLng(intent.extras.getDouble("arrival_latitude"),
                intent.extras.getDouble("arrival_longitude"))

        var options = MarkerOptions()
        options.position(departureLocation)
        options.icon(BitmapDescriptorFactory.fromBitmap(imagePlace))
        options.title(getString(R.string.bic_commons_departure))
        options.anchor(0.25f, 1f)
        val departureMarker = googleMap?.addMarker(options)
        options = MarkerOptions()
        options.position(arrivalLocation)
        options.icon(BitmapDescriptorFactory.fromBitmap(imagePlace))
        options.title(getString(R.string.bic_commons_arrival))
        options.anchor(0.25f, 1f)
        val arrivalMarker = googleMap?.addMarker(options)

        val vBoundsBuilder = LatLngBounds.Builder()
        vBoundsBuilder.include(departureMarker?.position)
        vBoundsBuilder.include(arrivalMarker?.position)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(vBoundsBuilder
                .build(), 150))
    }

    override fun onMapRefreshed(hasLocationPermissions: Boolean) {

    }

    override fun onUserLocationChanged(location: Location?) {

    }

    override fun onMarkerClicked(marker: Marker) {

    }

    override fun onCameraIdle() {

    }

}
