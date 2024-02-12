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
import java.util.Locale


class NotificationService : NotificationListenerService() {
    var context: Context? = null

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val notificationRepository by lazy { StatusBarNotificationRepository(database.statusBarNotificationDao()) }
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        getCurrentNotifications()
  val ff= activeNotifications
        for (f in ff){
            Log.i("Msg", "allNotifications getActiveNotifications f :$f")
            Log.i("Msg", "allNotifications getActiveNotifications f :$f")
        }
        Log.i("Msg", "allNotifications getActiveNotifications:$ff")
        Log.i("Msg", "allNotifications getActiveNotifications:$ff")

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

    private val pkgLastNotificationWhen: MutableMap<String, Long> = HashMap()
    override fun onNotificationPosted(sbn: StatusBarNotification) {

        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            Log.d("TAG", "Ignore the notification FLAG_GROUP_SUMMARY")
            return
        }

        val lastWhen = pkgLastNotificationWhen[sbn.packageName]
        if (lastWhen != null && lastWhen >= sbn.notification.`when`) {
            Log.d("TAG", "Ignore Old notification")
            return
        }
        pkgLastNotificationWhen[sbn.packageName] = sbn.notification.`when`


        val pack = sbn.packageName
        var ticker = ""
        if (sbn.notification.tickerText != null) {
            ticker = sbn.notification.tickerText.toString()
        }
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text").toString()
        val subtext = extras.getCharSequence("android.subText").toString()


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


        serviceScope.launch {
            if (!pack.equals(packageName)) {
               // val time = Calendar.getInstance().time
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val current = formatter.format(sbn.postTime)
                val newNotification = StatusBarNotificationEntity(
                    packageName = pack,
                    ticker = ticker,
                    title = title.toString(),
                    text = text.toString(),
                    subtext = subtext,
                    datetime = current
                    //  extras = convertBundleToString(extras),
                    //  sbn = sbn
                )
                notificationRepository.insert(newNotification)
            }
        }
       // LocalBroadcastManager.getInstance(context!!).sendBroadcast(msgrcv)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i("Msg", "Notification Removed")
    }

}