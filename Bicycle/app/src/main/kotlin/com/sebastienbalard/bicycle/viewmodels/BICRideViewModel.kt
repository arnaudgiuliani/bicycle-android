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

package com.sebastienbalard.bicycle.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sebastienbalard.bicycle.extensions.distanceTo
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICPlace
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class BICRideViewModel(private val contractRepository: BICContractRepository) : ViewModel() {

    companion object : SBLog()

    private val disposables: CompositeDisposable = CompositeDisposable()

    lateinit var departure: BICPlace
    lateinit var arrival: BICPlace
    var bikesCount: Int = 1
    var freeSlotsCount: Int = 1
    var departureNearestStations = MutableLiveData<List<BICStation>>()
    var arrivalNearestStations =  MutableLiveData<List<BICStation>>()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun determineNearestStations() {

        var radius = departure.location.distanceTo(arrival.location)
        if (radius > 500f) {
            radius = 500f
        }
        disposables.add(contractRepository.refreshStationsFor(departure.contract!!)
                .toObservable().flatMap { stations -> Observable.fromIterable(stations) }
                .filter { station -> station.location.distanceTo(departure.location) <= radius && station.availableBikesCount >= bikesCount }
                .toSortedList { station1, station2 ->
                    station1.location.distanceTo(departure.location).compareTo(station2.location.distanceTo(departure.location))
                }
                .doOnSuccess { stations -> v("take 3 nearest on ${stations.size}") }
                .toObservable().take(3)
                .observeOn(Schedulers.computation())
                .subscribe( {
                    stations -> departureNearestStations.value = stations
                }, {
                    _ -> departureNearestStations.value = null
                })
        )
        disposables.add(contractRepository.refreshStationsFor(arrival.contract!!)
                .toObservable().flatMap { stations -> Observable.fromIterable(stations) }
                .filter { station -> station.location.distanceTo(arrival.location) <= radius && station.freeStandsCount >= freeSlotsCount }
                .toSortedList { station1, station2 ->
                    station1.location.distanceTo(arrival.location).compareTo(station2.location.distanceTo(arrival.location))
                }
                .doOnSuccess { stations -> v("take 3 nearest on ${stations.size}") }
                .toObservable().take(3)
                .observeOn(Schedulers.computation())
                .subscribe( {
                    stations -> arrivalNearestStations.value = stations
                }, {
                    _ -> arrivalNearestStations.value = null
                })
        )
    }

}
