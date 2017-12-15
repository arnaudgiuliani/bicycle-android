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

import com.google.gson.GsonBuilder
import com.sebastienbalard.bicycle.io.dtos.CBContractResponseDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface CityBikesApi {

    companion object {
        val instance: CityBikesApi by lazy {

            // gson
            val gson = GsonBuilder().serializeNulls().create()

            // logger
            val vInterceptor = HttpLoggingInterceptor()
            vInterceptor.level = HttpLoggingInterceptor.Level.BODY

            // http client
            val httpClient = OkHttpClient.Builder().addInterceptor(vInterceptor).build()

            // retrofit
            val retrofit = Retrofit.Builder().client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl("http://api.citybik.es/v2/networks/")
                    .build()
            retrofit.create(CityBikesApi::class.java)
        }
    }

    @GET("{contract_name}?fields=stations")
    fun getStations(@Path("contract_name") name: String): Call<CBContractResponseDto>
}