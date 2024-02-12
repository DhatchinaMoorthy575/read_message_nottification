package com.test.internalapp.rest


import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import com.test.internalapp.model.DummyResponse
import com.test.internalapp.model.RequestMessageModelNew
import com.test.internalapp.model.RequestNotificationModelNew
import retrofit2.Call
import retrofit2.http.*

interface RestService {

    @POST(ApiUrls.TEST)
    fun sendMessageData(@Body data: List<RequestMessageModelNew>): Call<DummyResponse>

    @POST(ApiUrls.TEST)
    fun sendNotificationData(@Body data: List<RequestNotificationModelNew>): Call<DummyResponse>
}