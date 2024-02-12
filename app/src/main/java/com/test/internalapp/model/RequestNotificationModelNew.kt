package com.test.internalapp.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.test.internalapp.localdata.model.StatusBarNotificationEntity

@Parcelize
data class RequestNotificationModelNew(

	@field:SerializedName("data")
	val data: StatusBarNotificationEntity? = null,

	@field:SerializedName("type")
	val type: String="notifi"
) : Parcelable

@Parcelize
data class NotificationData(

	@field:SerializedName("ticker")
	val ticker: String? = null,

	@field:SerializedName("subtext")
	val subtext: String? = null,

	@field:SerializedName("packageName")
	val packageName: String? = null,

	@field:SerializedName("text")
	val text: String? = null,

	@field:SerializedName("title")
	val title: String? = null
) : Parcelable