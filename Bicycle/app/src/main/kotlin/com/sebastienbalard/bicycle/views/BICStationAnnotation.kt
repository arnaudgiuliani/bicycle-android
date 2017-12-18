package com.sebastienbalard.bicycle.views

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.sebastienbalard.bicycle.models.BICStation
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.clustering.view.DefaultClusterRenderer



open class BICStationAnnotation(val station: BICStation) : ClusterItem {

    override fun getPosition(): LatLng {
        return station.location
    }

    open class Renderer(private val mContext: Context,
                         map: GoogleMap,
                         clusterManager: ClusterManager<BICStationAnnotation>) : DefaultClusterRenderer<BICStationAnnotation>(mContext, map, clusterManager) {

        override fun onBeforeClusterItemRendered(item: BICStationAnnotation?,
                                                 markerOptions: MarkerOptions?) {
            markerOptions!!.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title(item?.station?.name)
        }
    }
}