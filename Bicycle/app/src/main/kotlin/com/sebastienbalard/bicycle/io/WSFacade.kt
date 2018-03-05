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

package com.sebastienbalard.bicycle.io

import com.sebastienbalard.bicycle.io.dtos.CBContractResponseDto
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WSFacade {

    companion object: SBLog() {

        fun getStationsByContract(contract: BICContract): Single<List<BICStation>> {
            val contractName = contract.url.substring(contract.url.lastIndexOf('/') + 1)
            d("contract endpoint: $contractName")
            return CityBikesApi.instance.getStations(contractName).map { response -> response.network.stations }
        }

        /*fun getStationsByContract(contract: BICContract, success: (List<BICStation>?) -> Unit, failure: (Throwable?) -> Unit) {
            val contractName = contract.url.substring(contract.url.lastIndexOf('/') + 1)
            d("contract endpoint: $contractName")
            CityBikesApi.instance.getStations(contractName).enqueue(object : Callback<CBContractResponseDto> {

                override fun onResponse(call: Call<CBContractResponseDto>?, response: Response<CBContractResponseDto>?) {
                    response?.let {
                        if (it.isSuccessful) {
                            it.body()?.let {
                                success(it.network.stations)
                            }
                        } else {
                            failure(RuntimeException("network error - code ${it.code()}"))
                        }
                    }
                }

                override fun onFailure(call: Call<CBContractResponseDto>?, t: Throwable?) {
                    failure(t)
                }
            })
        }*/
    }
}