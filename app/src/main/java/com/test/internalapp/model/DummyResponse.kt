package com.test.internalapp.model


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class DummyResponse(
    @SerializedName("data")
    var `data`: String,
    @SerializedName("message")
    var message: String,
    @SerializedName("status")
    var status: Boolean
) : Parcelable