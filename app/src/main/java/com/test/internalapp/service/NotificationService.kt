package com.test.internalapp.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmapOrNull
import com.test.internalapp.localdata.AppDatabase
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import com.test.internalapp.localdata.repository.StatusBarNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class NotificationService : NotificationListenerService() {
    var context: Context? = null

    override fun getActiveNotifications(): Array<StatusBarNotification> {
        return super.getActiveNotifications()
    }

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val notificationRepository by lazy { StatusBarNotificationRepository(database.statusBarNotificationDao()) }
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
/*        serviceScope.launch {
            // Example usage in Service
            notificationRepository.allNotifications.collect { notifications ->
                // Handle the list of notifications here
                // This code runs on the main thread, so update your UI or perform other operations
                Log.i("Msg", "allNotifications collect:$notifications")
            }
        }*/
        getCurrentNotifications()
  val ff= activeNotifications
        for (f in ff){
            Log.i("Msg", "allNotifications getActiveNotifications f :$f")
            Log.i("Msg", "allNotifications getActiveNotifications f :$f")
        }
        Log.i("Msg", "allNotifications getActiveNotifications:$ff")
        Log.i("Msg", "allNotifications getActiveNotifications:$ff")

/*
        // Delete all notifications (example)
        serviceScope.launch {
            notificationRepository.deleteAll()
        }*/
    }
    private fun getCurrentNotifications() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications

        for (notification in activeNotifications) {
            // Process each active notification
            val notificationText = extractNotificationText(notification.notification)
            Log.d("MainActivity", "Active notification: $notificationText")
        }
    }

    private fun extractNotificationText(notification: Notification): String {
        // Extract relevant information from the notification
        val title = notification.extras?.getString(Notification.EXTRA_TITLE) ?: ""
        val text = notification.extras?.getString(Notification.EXTRA_TEXT) ?: ""
        return "$title: $text"
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pack = sbn.packageName
        var ticker = ""
        if (sbn.notification.tickerText != null) {
            ticker = sbn.notification.tickerText.toString()
        }
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text").toString()
        val subtext = extras.getCharSequence("android.subText").toString()

        val smallIcon = sbn.notification.smallIcon
        var smallIconbitmap: Bitmap?=null
        if(smallIcon!=null) {
            val drawable: Drawable? = smallIcon.loadDrawable(context)
            smallIconbitmap = drawable?.toBitmapOrNull()
        }
        val largeIcon = sbn.notification.getLargeIcon()
        var largeIconbitmap: Bitmap?=null
        if(largeIcon!=null) {
            val largeIcondrawable: Drawable? = largeIcon.loadDrawable(context)
            largeIconbitmap = largeIcondrawable?.toBitmapOrNull()
        }


        if (extras.containsKey(Notification.EXTRA_PICTURE)) {
            // this bitmap contain the picture attachment
            val bmp = extras[Notification.EXTRA_PICTURE] as Bitmap?
        }

        Log.i("Package", pack)
        Log.i("Ticker", ticker)
        Log.i("Title", title.toString())
        Log.i("Text", text.toString())
        val msgrcv = Intent("notification_updated")
        msgrcv.putExtra("package", pack)
        msgrcv.putExtra("ticker", ticker)
        msgrcv.putExtra("title", title)
        msgrcv.putExtra("text", text)
        msgrcv.putExtra("subtext", subtext)



  /*      if (id != null) {
            val stream = ByteArrayOutputStream()
            id.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            msgrcv.putExtra("icon", byteArray)
        }*/
        // Insert a new notification (example)
        serviceScope.launch {
            if (!pack.equals(packageName)) {
                val time = Calendar.getInstance().time
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val current = formatter.format(time)
                val newNotification = StatusBarNotificationEntity(
                    packageName = pack,
                    ticker = ticker,
                    title = title.toString(),
                    text = text.toString(),
                    subtext = subtext,
                   smallIconId = smallIconbitmap,
                    largeIcon = largeIconbitmap,
                    datetime = current
                    //  extras = convertBundleToString(extras),
                    //  sbn = sbn
                )
                notificationRepository.insert(newNotification)
            }
        }
       // LocalBroadcastManager.getInstance(context!!).sendBroadcast(msgrcv)
    }
/*    fun convertBundleToString(bundle: Bundle): String {
        val json = JSONObject()

        // Iterate through all keys in the Bundle and add them to the JSON object
        for (key in bundle.keySet()) {
            val value = bundle.get(key)
            json.put(key, value)
        }

        // Convert the JSON object to a string
        return json.toString()
    }*/
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i("Msg", "Notification Removed")
    }

}