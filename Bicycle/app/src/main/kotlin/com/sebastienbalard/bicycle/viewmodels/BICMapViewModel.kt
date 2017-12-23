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

package com.sebastienbalard.bicycle.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sebastienbalard.bicycle.BICApplication
import com.sebastienbalard.bicycle.extensions.distanceTo
import com.sebastienbalard.bicycle.extensions.intersect
import com.sebastienbalard.bicycle.io.WSFacade
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.models.SBLocationLiveData
import java.io.IOException
import java.nio.charset.Charset

class BICMapViewModel(application: Application) : AndroidViewModel(application) {

    companion object : SBLog()

    var userLocation = SBLocationLiveData(getApplication())
    var allContracts = ArrayList<BICContract>()
    var currentContract = MutableLiveData<BICContract>()
    var hasCurrentContractChanged = MutableLiveData<Boolean>()
    var currentStations = MutableLiveData<List<BICStation>>()
    private var cacheStations = HashMap<String, List<BICStation>>()

    init {
        loadContracts()
    }

    fun loadCurrentContractStations() {
        if (cacheStations.containsKey(currentContract.value!!.name)) {
            currentStations.value = cacheStations.getValue(currentContract.value!!.name)
        } else {
            refreshContractStations(currentContract.value!!)
        }
    }

    fun refreshContractStations(contract: BICContract) {
        WSFacade.getStationsByContract(contract, success = {
            it?.let {
                cacheStations.set(contract.name, it)
                currentStations.value = it
            }
        }, failure = {
            currentStations.value = null
        })
    }

    fun determineCurrentContract(visibleBounds: LatLngBounds) {

        var invalidateCurrentContract = false
        var hasChanged = false
        var current = currentContract.value

        current?.let {
            if (!it.bounds.intersect(visibleBounds)) {
                invalidateCurrentContract = true
                hasChanged = true
                current = null
            }
        }

        if (current == null || invalidateCurrentContract) {
            current = getContractFor(visibleBounds.center)
            hasChanged = hasChanged || current != null
        }

        hasCurrentContractChanged.value = hasChanged
        if (currentContract.value != null && current != null) {
            if (!currentContract.value!!.equals(current!!)) {
                // current has changed
                currentContract.value = current
            }
        } else {
            // someone is null
            currentContract.value = current
        }
    }

    private fun getContractFor(latLng: LatLng): BICContract? {
        val filteredList = allContracts.filter { contract -> contract.bounds.contains(latLng) }
        var rightContract: BICContract? = null
        if (filteredList.isNotEmpty()) {
            rightContract = filteredList.first()
            if (filteredList.size > 1) {
                var minDistance: Float? = null
                var distanceFromCenter: Float?
                for (filtered in filteredList) {
                    distanceFromCenter = latLng.distanceTo(filtered.center)
                    if (minDistance == null) {
                        minDistance = distanceFromCenter
                        rightContract = filtered
                    } else if (minDistance > distanceFromCenter) {
                        minDistance = distanceFromCenter
                        rightContract = filtered
                    }
                }
            }
        }
        return rightContract
    }

    private fun loadContracts() {
        try {
            val inputStream = getApplication<BICApplication>().assets.open("contracts.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charset.forName("UTF-8"))

            allContracts = Gson().fromJson(json, object : TypeToken<ArrayList<BICContract>>() {}.type)
            d("${allContracts.size} contracts loaded")
        } catch (exception: IOException) {
            e("fail to load contracts from assets", exception)
        }
    }
}