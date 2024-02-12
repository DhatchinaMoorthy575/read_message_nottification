package com.test.internalapp.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity

@Parcelize
data class RequestMessageModelNew(

    @field:SerializedName("data")
    val data: MessageEntity? = null,

    @field:SerializedName("type")
    val type: String="msg"
) : Parcelable

@Parcelize
data class MessageData(

    @field:SerializedName("date")
    val date: String? = null,

    @field:SerializedName("sender")
    val sender: String? = null,

    @field:SerializedName("message")
    val message: String? = null
) : Parcelable