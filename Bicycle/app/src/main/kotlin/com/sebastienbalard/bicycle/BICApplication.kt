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

package com.sebastienbalard.bicycle

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.sebastienbalard.bicycle.di.bicycleApp
import com.sebastienbalard.bicycle.misc.SBLog
import org.koin.android.ext.android.startKoin

class BICApplication : Application() {

    companion object: SBLog() {

        private lateinit var instance: BICApplication

        val context: Context
            get() = instance.applicationContext
        val resources: Resources
            get() = instance.resources
    }

    override fun onCreate() {
        super.onCreate()
        v("onCreate")
        startKoin(this, bicycleApp)
        instance = this
    }

}