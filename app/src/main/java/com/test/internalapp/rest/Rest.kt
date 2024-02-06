package com.test.internalapp.rest

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import com.test.internalapp.model.DummyResponse
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Response


class Rest(var ctx: Context, var callback: Callback<*>) {
    companion object {
        private val TAG = Rest::class.java.name

        fun printLog(msg: String?) {
            Log.e("Log: ", msg!!)
        }

    }

    private var restService: RestService? = null
    private lateinit var gson: Gson

    init {
        init()
    }

    private fun init() {
        restService = RestAdapter.adapter
        gson = Gson()
    }

    fun OnFailedResponse(response: Response<*>, context: Context) {
        try {
            val jObjError = JSONObject(response.errorBody()!!.string())
            Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, " Error Code: " + response.code() + "\n Message: " + response.message(), Toast.LENGTH_LONG).show()
        }
    }

    fun onFailedToast(text: String, context: Context) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun OnFailedResponseData(response: Response<*>, context: Context) {
        try {
            val jObjError = JSONObject(response.errorBody()!!.string())
            //UtiL.showToast(context, jObjError.getString("data"))
            Toast.makeText(context, jObjError.getString("data"), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
        }
    }


    fun sendMessageData(messageentity:  ArrayList<MessageEntity>) {
        Log.e(TAG, "sendMessageData :: messageentity:$messageentity")

        RestAdapter.adapter.sendMessageData(messageentity).enqueue(callback as Callback<DummyResponse>)
    }

    fun sendNotificationData(notifications: ArrayList<StatusBarNotificationEntity>) {
        Log.e(TAG, "sendNotificationData :: notifications:$notifications")
        RestAdapter.adapter.sendNotificationData(notifications).enqueue(callback as Callback<DummyResponse>)
    }
}