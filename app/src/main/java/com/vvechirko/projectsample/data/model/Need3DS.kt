package com.vvechirko.projectsample.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Need3DS(
    @SerializedName("shop_bill_id")
    val shopBillId: String,
    val action: String,
    val pareq: String,
    val md: String
) : Parcelable