package com.vvechirko.projectsample.data.model

import com.google.gson.annotations.SerializedName

class DeliveryAddress(
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Float,
    @SerializedName("longitude")
    val longitude: Float
) {
    fun toLocationPoint(): LocationPoint {
        return LocationPoint(latitude, longitude)
    }
}