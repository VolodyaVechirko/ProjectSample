package com.vvechirko.projectsample.domain

import com.vvechirko.projectsample.data.api.PlacesApi
import com.vvechirko.projectsample.data.model.LocationPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

const val PLACES_KEY = "KEY"

class PlacesInteractor(
    val placesApi: PlacesApi
) {
    private var sessionToken = newToken()

    private fun newToken() = UUID.randomUUID().toString()

    suspend fun autoComplete(input: String) = withContext(Dispatchers.IO) {
        val query = mapOf(
            "key" to PLACES_KEY,
            "input" to input,
            "types" to "address",
            "location" to "49.8397,24.0297", // Lviv center location
            "radius" to 20000,
            "strictbounds" to true,
            "components" to "country:ua",
            "language" to "uk-UA",
            "sessiontoken" to sessionToken
        )
        placesApi.autoComplete(query).predictions
    }

    suspend fun placeDetails(placeId: String) = withContext(Dispatchers.IO) {
        val query = mapOf(
            "key" to PLACES_KEY,
            "placeid" to placeId,
            "fields" to "address_components,geometry/location",
            "language" to "uk-UA",
            "sessiontoken" to sessionToken
        )
        placesApi.placeDetails(query).also {
            sessionToken = newToken() // refresh token after get place details
        }.result
    }

    suspend fun geocodeLocation(point: LocationPoint) = withContext(Dispatchers.IO) {
        val query = mapOf(
            "key" to PLACES_KEY,
            "latlng" to point.toString(),
            "result_type" to "street_address"
        )
        placesApi.geocodeLocation(query).results.firstOrNull()
    }

    suspend fun getRecent() = withContext(Dispatchers.IO) {
        // TODO
    }
}