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

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.sebastienbalard.bicycle.BuildConfig
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.*
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICPlace
import com.sebastienbalard.bicycle.viewmodels.BICHomeViewModel
import com.sebastienbalard.bicycle.viewmodels.BICSearchViewModel
import kotlinx.android.synthetic.main.bic_activity_home.*
import kotlinx.android.synthetic.main.bic_widget_appbar.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.run
import org.koin.android.architecture.ext.viewModel
import java.util.*
import kotlin.concurrent.timerTask
import android.content.Intent


class BICHomeActivity : SBMapActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICHomeActivity::class.java)
        }
    }

    private val viewModelHome: BICHomeViewModel by viewModel()
    private val viewModelSearch: BICSearchViewModel by viewModel()

    private var clusterManager: ClusterManager<BICStationAnnotation>? = null
    private var listContractsAnnotations: MutableList<Marker>? = null
    private var timer: Timer? = null
    private var menuItemSearch: MenuItem? = null

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_home)
        v("onCreate")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        listContractsAnnotations = mutableListOf()

        initMap(savedInstanceState)
        initSearchView()

        //layoutSearch.animate().translationYBy(0f).translationY(layoutSearch.measuredHeight.toFloat() * -1).start()
    }

    override fun onStart() {
        super.onStart()
        v("onStart")
        startTimer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        v("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.bic_menu_home, menu)
        menuItemSearch = menu?.findItem(R.id.bic_menu_home_item_search)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.bic_menu_home_item_search -> {
                i("click on menu item: search")
                showLayoutSearch()
                true
            }
            R.id.bic_menu_home_item_favorites -> {
                i("click on menu item: favorites")
                Toast.makeText(this, R.string.bic_messages_available_soon, Toast.LENGTH_LONG).showAsError(this)
                true
            }
            R.id.bic_menu_home_item_about -> {
                i("click on menu item: about")
                Toast.makeText(this, R.string.bic_messages_available_soon, Toast.LENGTH_LONG).showAsError(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        v("onResume")
        viewModelHome.hasCurrentContractChanged.observe(this, Observer { hasChanged ->
            hasChanged?.let {
                if (!it) {
                    v("current contract has not changed")
                    // reload clustering
                    clusterManager?.cluster()
                }
            }
        })
        viewModelHome.currentContract.observe(this, Observer { contract ->
            if (contract == null) {
                d("current bounds is out of contracts covers")
                stopTimer()
            } else {
                stopTimer()
                d("refresh contract stations: ${contract.name} (${contract.provider.tag})")
                // refresh current contract stations data
                viewModelHome.loadCurrentContractStations()
                startTimer()
            }
        })
        viewModelHome.currentStations.observe(this, Observer { stations ->
            clusterManager?.clearItems()
            stations?.map { station ->
                clusterManager?.addItem(BICStationAnnotation(station))
            }
            if (stations ==  null) {
                showErrorForCurrentContractStation()
            }
            clusterManager?.cluster()
        })
        viewModelSearch.isSearchButtonEnabled.observe(this, Observer { enabled ->
            enabled?.let {
                buttonSearch.isEnabled = it
            }
        })
    }

    override fun onPause() {
        super.onPause()
        v("onPause")
        viewModelHome.currentContract.removeObservers(this)
    }

    override fun onStop() {
        super.onStop()
        v("onStop")
        stopTimer()
        hideSoftInput()
    }

    //endregion

    //region Map events

    override fun onMapInitialized() {
        clusterManager = ClusterManager(this, googleMap!!)
        clusterManager?.renderer = BICStationAnnotation.Renderer(this, googleMap!!, clusterManager!!)
        googleMap!!.setOnInfoWindowClickListener(clusterManager)
    }

    override fun onMapRefreshed(hasLocationPermissions: Boolean) {
        if (hasLocationPermissions) {
            buttonLocalizeDeparture.visibility = View.VISIBLE
            buttonLocalizeArrival.visibility = View.VISIBLE
        } else {
            buttonLocalizeDeparture.visibility = View.GONE
            buttonLocalizeArrival.visibility = View.GONE
        }
    }

    override fun onUserLocationChanged(location: Location?) {
        location?.let {
            v("move camera to new user location")
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
            googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f), 1000, null)
        }
    }

    override fun onMarkerClicked(marker: Marker) {

    }

    override fun onCameraIdle() {
        refreshMarkers()
    }

    //endregion

    //region Private methods

    private fun hideLayoutSearch() {
        val params = layoutSearch.layoutParams as CoordinatorLayout.LayoutParams
        params.topMargin -= layoutSearch.measuredHeight
        layoutSearch.layoutParams = params
        menuItemSearch?.isVisible = true
        val animate = RotateAnimation(180f, 360f, imageViewCollapse.measuredWidth / 2f, imageViewCollapse.measuredHeight / 2f)
        animate.duration = 200
        animate.fillAfter = true
        imageViewCollapse.startAnimation(animate)
        hideSoftInput()
    }

    private fun showLayoutSearch() {
        menuItemSearch?.isVisible = false
        /*val animate = TranslateAnimation(
                0f,
                0f,
                0f,
                layoutSearch.measuredHeight.toFloat())
        animate.duration = 400
        animate.fillAfter = true
        layoutSearch.startAnimation(animate)*/
        val params = layoutSearch.layoutParams as CoordinatorLayout.LayoutParams
        params.topMargin += layoutSearch.measuredHeight
        layoutSearch.layoutParams = params
        val animate = RotateAnimation(0f, 180f, imageViewCollapse.measuredWidth / 2f, imageViewCollapse.measuredHeight / 2f)
        animate.duration = 200
        animate.fillAfter = true
        imageViewCollapse.startAnimation(animate)
        showSoftInput()
    }

    private fun showErrorForCurrentContractStation() {
        val snackbar = Snackbar.make(toolbar, R.string.bic_messages_warning_get_current_contract_stations, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.bic_color_red))
        val textView = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
        textView.setTextColor(ContextCompat.getColor(this, R.color.bic_color_white))
        snackbar.show()
    }

    private fun startTimer() {
        val zoomLevel = googleMap?.cameraPosition?.zoom?.toInt()
        if (timer == null && viewModelHome.currentContract.value != null && zoomLevel != null && zoomLevel >= 10) {
            val delay = BuildConfig.TIME_BEFORE_REFRESH_STATIONS_DATA_IN_SECONDS * 1000
            d("start timer")
            timer = Timer()
            timer!!.scheduleAtFixedRate(timerTask {
                d("timer fired")
                viewModelHome.currentContract.value?.let {
                    viewModelHome.refreshContractStations(it)
                }
            }, delay, delay)
        }
    }

    private fun stopTimer() {
        timer?.let {
            d("stop timer")
            it.cancel()
            timer = null
        }
    }

    private fun refreshMarkers() {
        val level = googleMap?.cameraPosition?.zoom?.toInt()
        level?.let {
            d("current zoom level: $level")
            if (it >= 10) {
                deleteContractsAnnotations()
                viewModelHome.determineCurrentContract(googleMap!!.projection.visibleRegion.latLngBounds)
            } else {
                viewModelHome.currentContract.value = null
                stopTimer()
                createContractsAnnotations()
            }
            (autoCompleteTextViewDepartureAddress.adapter as BICPlacesAutoCompleteAdapter).bounds = googleMap!!.projection.visibleRegion.latLngBounds
            (autoCompleteTextViewArrivalAddress.adapter as BICPlacesAutoCompleteAdapter).bounds = googleMap!!.projection.visibleRegion.latLngBounds
        }
    }

    private fun deleteContractsAnnotations() {
        listContractsAnnotations?.let {
            if (it.size > 0) {
                d("delete contracts annotations")
                it.map { marker -> marker.remove() }
                it.clear()
            }
        }
    }

    private fun createContractsAnnotations() {
        d("create contracts annotations")
        val size = resources.getDimensionPixelSize(R.dimen.bic_size_annotation)
        val imageContract = getBitmapDescriptor(R.drawable.bic_img_contract, size, size)
        val hasMarkers = clusterManager?.markerCollection?.markers?.isNotEmpty()?.or(false)!!
        val hasClusterMarkers = clusterManager?.clusterMarkerCollection?.markers?.isNotEmpty()?.or(false)!!
        if (hasMarkers || hasClusterMarkers) {
            clusterManager?.clearItems()
            clusterManager?.cluster()
        }
        if (listContractsAnnotations?.size == 0) {
            async(CommonPool) {
                var options: MarkerOptions
                viewModelHome.allContracts.map { contract ->
                    options = MarkerOptions()
                    options.position(contract.center)
                    options.icon(imageContract)
                    options.title(contract.name)
                    run(UI) {
                        googleMap?.addMarker(options)?.let {
                            listContractsAnnotations?.add(it)
                        }
                    }
                }
                run(UI) {
                    // do this to avoid contracts partial display after activity launch
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

    @SuppressLint("ShowToast")
    private fun initSearchView() {
        autoCompleteTextViewDepartureAddress.setAdapter(BICPlacesAutoCompleteAdapter(this))
        autoCompleteTextViewDepartureAddress.setOnClearListener {
            viewModelSearch.departure = null
        }
        autoCompleteTextViewDepartureAddress.setOnItemClickListener { _, _, _, _ ->
            geocode(autoCompleteTextViewDepartureAddress.text.toString().trim())?.let {
                val place = BICPlace(it)
                place.contract = viewModelHome.getContractFor(it)
                v("set departure place with autocompletion (${place.contract?.name})")
                viewModelSearch.departure = place
                editTextDepartureBikesCount.requestFocus()
            } ?: autoCompleteTextViewDepartureAddress.setText("")
        }
        buttonLocalizeDeparture.setOnClickListener {
            viewModelMap.userLocation.value?.let {
                autoCompleteTextViewDepartureAddress.text.clear()
                val place = BICPlace(it)
                place.contract = viewModelHome.getContractFor(it)
                v("set departure place with user location (${place.contract?.name})")
                viewModelSearch.departure = place
                autoCompleteTextViewDepartureAddress.setText(geocodeReverse(viewModelSearch.departure!!.location))
                editTextDepartureBikesCount.requestFocus()
            }
        }
        editTextDepartureBikesCount.append("1")
        editTextDepartureBikesCount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                v("set bikes count")
                var count = 1
                val text = editTextDepartureBikesCount.text.toString().trim()
                if (!text.isEmpty()) {
                    val input = text.toInt()
                    if (input > 0) {
                        count = input
                    } else {
                        editTextDepartureBikesCount.setText("")
                        editTextDepartureBikesCount.append("1")
                    }
                } else {
                    editTextDepartureBikesCount.setText("")
                    editTextDepartureBikesCount.append("1")
                }
                viewModelSearch.bikesCount = count
            }
        }
        autoCompleteTextViewArrivalAddress.setAdapter(BICPlacesAutoCompleteAdapter(this))
        autoCompleteTextViewArrivalAddress.setOnClearListener {
            viewModelSearch.arrival = null
        }
        autoCompleteTextViewArrivalAddress.setOnItemClickListener { _, _, _, _ ->
            geocode(autoCompleteTextViewArrivalAddress.text.toString().trim())?.let {
                val place = BICPlace(it)
                place.contract = viewModelHome.getContractFor(it)
                v("set arrival place with autocompletion (${place.contract?.name})")
                viewModelSearch.arrival = place
                editTextArrivalFreeSlotsCount.requestFocus()
            } ?: autoCompleteTextViewArrivalAddress.setText("")
        }
        buttonLocalizeArrival.setOnClickListener {
            viewModelMap.userLocation.value?.let {
                autoCompleteTextViewArrivalAddress.text.clear()
                val place = BICPlace(it)
                place.contract = viewModelHome.getContractFor(it)
                v("set arrival place with user location (${place.contract?.name})")
                viewModelSearch.arrival = place
                autoCompleteTextViewArrivalAddress.setText(geocodeReverse(viewModelSearch.arrival!!.location))
                editTextArrivalFreeSlotsCount.requestFocus()
            }
        }
        editTextArrivalFreeSlotsCount.append("1")
        editTextArrivalFreeSlotsCount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                v("set free slots count")
                var count = 1
                val text = editTextArrivalFreeSlotsCount.text.toString().trim()
                if (!text.isEmpty()) {
                    val input = text.toInt()
                    if (input > 0) {
                        count = input
                    } else {
                        editTextArrivalFreeSlotsCount.setText("")
                        editTextArrivalFreeSlotsCount.append("1")
                    }
                } else {
                    editTextArrivalFreeSlotsCount.setText("")
                    editTextArrivalFreeSlotsCount.append("1")
                }
                viewModelSearch.freeSlotsCount = count
            }
        }
        buttonSearch.setOnClickListener {
            i("click on button: search")
            if (viewModelSearch.isComplete) {
                hideLayoutSearch()
                startActivity(BICRideActivity.getIntentForRide(this, viewModelSearch.departure!!.location.latitude,
                        viewModelSearch.departure!!.location.longitude, viewModelSearch.arrival!!.location.latitude,
                        viewModelSearch.arrival!!.location.longitude))
            } else {
                Toast.makeText(this, R.string.bic_messages_warning_not_the_same_contract, Toast.LENGTH_LONG).showAsError(this)
            }
        }
        layoutCollapse.setOnClickListener { _ ->
            i("click on layout: collapse")
            hideLayoutSearch()
        }
    }

    //endregion
}
