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
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.*
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.Tasks
import com.sebastienbalard.bicycle.misc.SBLog
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class BICPlacesAutoCompleteAdapter(context: Context, resource: Int = android.R.layout.simple_expandable_list_item_2, textViewResourceId: Int = android.R.id.text1) : ArrayAdapter<AutocompletePrediction>(context, resource, textViewResourceId), Filterable {

    companion object : SBLog()

    private val geoDataClient: GeoDataClient = Places.getGeoDataClient(context, null)
    private val listResults: ArrayList<AutocompletePrediction> = ArrayList()
    var bounds: LatLngBounds? = null

    private fun getAutocomplete(constraint: CharSequence): ArrayList<AutocompletePrediction>? {
        v("start autocomplete query for: $constraint")

        // Submit the query to the autocomplete API and retrieve a PendingResult that will
        // contain the results when the query completes.
        val results = geoDataClient.getAutocompletePredictions(constraint.toString(), bounds,
                AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                        .build())

        // This method should have been called off the main UI thread. Block and wait for at most
        // 60s for a result from the API.
        try {
            Tasks.await<AutocompletePredictionBufferResponse>(results, 10, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }

        return try {
            val autocompletePredictions = results.result
            v("${autocompletePredictions.count} predictions found")
            // Freeze the results immutable representation that can be stored safely.
            DataBufferUtils.freezeAndClose(autocompletePredictions)
        } catch (e: RuntimeExecutionException) {
            // If the query did not complete successfully return null
            Toast.makeText(context, "Error contacting API: " + e.toString(),
                    Toast.LENGTH_SHORT).show()
            e("fail to get predictions", e)
            null
        }

    }

    //region ArrayAdapter

    override fun getCount(): Int {
        return listResults.size
    }

    override fun getItem(position: Int): AutocompletePrediction {
        return listResults[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val row = super.getView(position, convertView, parent)

        val item = getItem(position)
        
        val textView1 = row.findViewById(android.R.id.text1) as TextView
        val textView2 = row.findViewById(android.R.id.text2) as TextView
        textView1.text = item.getPrimaryText(StyleSpan(Typeface.BOLD))
        textView2.text = item.getSecondaryText(StyleSpan(Typeface.BOLD))

        return row
    }

    //endregion

    //region Filterable

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {

                val results = Filter.FilterResults()
                var filterData: ArrayList<AutocompletePrediction>? = ArrayList()

                // Skip the autocomplete query if no constraints are given.
                if (constraint != null && constraint.length > 3) {
                    // Query the autocomplete API for the (constraint) search string.
                    filterData = getAutocomplete(constraint)
                }

                results.values = filterData
                if (filterData != null) {
                    results.count = filterData.size
                } else {
                    results.count = 0
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {

                if (results != null && results.count > 0) {
                    listResults.clear()
                    listResults.addAll(results.values as ArrayList<AutocompletePrediction>)
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                return if (resultValue is AutocompletePrediction) {
                    resultValue.getFullText(null)
                } else {
                    super.convertResultToString(resultValue)
                }
            }
        }
    }

    //endregion
}