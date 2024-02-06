package com.test.internalapp.rest


import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import com.test.internalapp.model.DummyResponse
import retrofit2.Call
import retrofit2.http.*

interface RestService {

    @POST(ApiUrls.TEST)
    fun sendMessageData(@Body data: List<MessageEntity>): Call<DummyResponse>

    @POST(ApiUrls.TEST)
    fun sendNotificationData(@Body data: List<StatusBarNotificationEntity>): Call<DummyResponse>
}