package com.vvechirko.projectsample.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class LocationPoint(
    @SerializedName("lat")
    val latitude: Float,
    @SerializedName("lon")
    val longitude: Float
) : Parcelable {
    override fun toString(): String {
        return String.format(Locale.US, "%.8f,%.8f", latitude, longitude)
    }

    fun distanceTo(dest: LocationPoint): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            this.latitude.toDouble(), this.longitude.toDouble(),
            dest.latitude.toDouble(), dest.longitude.toDouble(), result
        )
        return result[0]
    }

    fun toLatLng() = LatLng(latitude.toDouble(), longitude.toDouble())
}