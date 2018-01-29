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

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.drawOn
import com.sebastienbalard.bicycle.extensions.getBitmap
import com.sebastienbalard.bicycle.models.BICStation


open class BICStationAnnotation(val station: BICStation) : ClusterItem {

    override fun getPosition(): LatLng {
        return station.location
    }

    open class Renderer(context: Context,
                         map: GoogleMap,
                         clusterManager: ClusterManager<BICStationAnnotation>) : DefaultClusterRenderer<BICStationAnnotation>(context, map, clusterManager) {

        private val size = context.resources.getDimensionPixelSize(R.dimen.bic_size_annotation)
        private val textSize = context.resources.getDimensionPixelSize(R.dimen.bic_size_font_body)
        private val imageStation = context.getBitmap(R.drawable.bic_img_station, size, size)
        private val colorWhite = ContextCompat.getColor(context, R.color.bic_color_white)

        override fun onBeforeClusterItemRendered(item: BICStationAnnotation?,
                                                 markerOptions: MarkerOptions?) {

            var icon = imageStation
            item?.station?.availableBikesCount?.let {
                icon = imageStation.drawOn(it.toString(), colorWhite, textSize.toFloat(), 3.75f)
            }
            item?.station?.freeStandsCount?.let {
                icon = icon.drawOn(it.toString(), colorWhite, textSize.toFloat(), 1.65f)
            }

            markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item?.station?.name)
        }
    }
}