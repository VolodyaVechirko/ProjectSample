package com.vvechirko.projectsample.data.api

import com.google.gson.annotations.SerializedName

class CompleteResult(
    @SerializedName("predictions")
    val predictions: List<PlaceComplete>
)

class GeocodeResult(
    @SerializedName("results")
    val results: List<PlaceDetails>
)

class PlaceResult(
    @SerializedName("result")
    val result: PlaceDetails
)

class PlaceComplete(
    val placeId: String,
    val mainText: String,
    val secondaryText: String
)

class PlaceDetails(
    val number: String?,
    val street: String?,
    val city: String?,
    val region: String?,

    val lat: Float,
    val lng: Float
) {
    // check whether Place contains street number
    val hasStreetNumber: Boolean
        get() = number != null && street != null

    val address: String
        get() = StringBuilder().apply {
            if (city != null) {
                append(city)
                append(", ")
                if (street != null) {
                    append(street)
                    append(", ")
                    if (number != null) {
                        append(number)
                    }
                }
            }
        }.toString()

    val mainText: String
        get() = StringBuilder().apply {
            if (street != null) {
                append(street)
                append(", ")
                if (number != null) {
                    append(number)
                }
            }
        }.toString()

    val secondaryText: String
        get() = StringBuilder().apply {
            if (city != null) {
                append(city)
                append(", ")
                if (region != null) {
                    append(region)
                }
            }
        }.toString()
}