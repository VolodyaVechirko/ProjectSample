package com.vvechirko.projectsample.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Building(
    val id: String,
    val title: String,
    val status: String,
    @SerializedName("image_url")
    val image: String?,
    val address: String,
    val radius: Int, // radius in meters
    val location: LocationPoint
) : Parcelable {

    @IgnoredOnParcel
    var distance: Float? = null

    val active: Boolean
        get() = status == Status.ACTIVE

    val distanceFormatted: String?
        get() {
            val d = distance ?: return null
            return if (d < 1000) {
                String.format(Locale.US, "%.0f m", d)
            } else {
                String.format(Locale.US, "%.1f km", d / 1000)
            }
        }

    object Status {
        const val ACTIVE = "active"
        const val NOT_ACTIVE = "not_active"
    }
}