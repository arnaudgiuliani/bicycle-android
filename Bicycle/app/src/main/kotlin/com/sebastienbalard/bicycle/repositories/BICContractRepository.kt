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

package com.sebastienbalard.bicycle.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sebastienbalard.bicycle.BICApplication
import com.sebastienbalard.bicycle.io.WSFacade
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import io.reactivex.Observable
import io.reactivex.Single
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class BICContractRepository {

    companion object : SBLog()

    var allContracts = ArrayList<BICContract>()
    private var cacheStations = HashMap<String, List<BICStation>>()

    init {
        loadContracts()
    }

    fun getStationsFor(contract: BICContract): Single<List<BICStation>> {
        return if (cacheStations.containsKey(contract.name)) {
            //Observable.fromIterable(cacheStations.getValue(contract.name))
            Single.fromObservable(Observable.fromArray(cacheStations.getValue(contract.name)))
        } else {
            refreshStationsFor(contract)
        }
    }

    fun refreshStationsFor(contract: BICContract): Single<List<BICStation>> {
        return WSFacade.getStationsByContract(contract)
                .doOnSuccess { stations -> cacheStations[contract.name] = stations }
                .doOnError { throwable -> e("fail to get contract stations", throwable) }
    }

    private fun loadContracts() {
        try {
            val inputStream = BICApplication.context.assets.open("contracts.json")
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
