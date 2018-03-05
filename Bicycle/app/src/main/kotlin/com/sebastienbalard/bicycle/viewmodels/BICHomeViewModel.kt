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
import android.location.Location
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
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.internal.util.ExceptionHelper
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.nio.charset.Charset

class BICHomeViewModel(application: Application, private val contractRepository: BICContractRepository) : AndroidViewModel(application) {

    companion object : SBLog()

    private val disposables: CompositeDisposable = CompositeDisposable()

    var currentContract = MutableLiveData<BICContract>()
    var hasCurrentContractChanged = MutableLiveData<Boolean>()
    var currentStations = MutableLiveData<List<BICStation>>()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun getAllContracts(): List<BICContract> {
        return contractRepository.allContracts
    }

    fun loadCurrentContractStations() {
        disposables.add(contractRepository.getStationsFor(currentContract.value!!).observeOn(Schedulers.computation()).subscribe({
            stations -> currentStations.value = stations
        }, { _ -> currentStations.value = null }))
    }

    fun refreshContractStations(contract: BICContract) {
        disposables.add(contractRepository.refreshStationsFor(contract).observeOn(Schedulers.computation()).subscribe({
            stations -> currentStations.value = stations
        }, { _ -> currentStations.value = null }))
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

    fun getContractFor(location: Location): BICContract? {
        return getContractFor(LatLng(location.latitude, location.longitude))
    }

    fun getContractFor(latLng: LatLng): BICContract? {
        val filteredList = contractRepository.allContracts.filter { contract -> contract.bounds.contains(latLng) }
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
}