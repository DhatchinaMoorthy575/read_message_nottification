package com.test.internalapp.rest


import com.test.internalapp.localdata.SharedPrefs
import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import com.test.internalapp.model.DummyResponse
import com.test.internalapp.model.RequestMessageModelNew
import com.test.internalapp.model.RequestNotificationModelNew
import com.test.internalapp.util.MyApp
import retrofit2.Call
import retrofit2.http.*

interface RestService {

    @POST()
    fun sendMessageData(@Url url: String, @Body data: List<RequestMessageModelNew>): Call<Any>

    @POST()
    fun sendNotificationData(@Url url: String,@Body data: List<RequestNotificationModelNew>): Call<Any>
}