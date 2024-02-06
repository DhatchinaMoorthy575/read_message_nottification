package com.test.internalapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.test.internalapp.localdata.SharedPrefs
import com.test.internalapp.service.MyForegroundService

class RestarterBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG + " -Restart ", "Service Stops!!!!!")
        if (intent != null && Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val isStartService = SharedPrefs.getStartService(context)
            if (isStartService) {
                Log.i("$TAG _MyService STOP and restart ", "isStartService: $isStartService")
                val myIntent = Intent(context, MyForegroundService::class.java)
                //if (!MyForegroundService.isMyForegroundServiceRunning) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startService(myIntent)
                    }
                    else {
                        context.startService(myIntent)
                    }
               // }
            }
            else {
                context.stopService(Intent(context, MyForegroundService::class.java))
                Log.i("$TAG _MyService STOP ", "No Restart!!!!!")
            }
            Log.i("$TAG _MyService STOP ", " ACTION_BOOT_COMPLETED $isStartService")
        }
        else {
            val isStartService = SharedPrefs.getStartService(context)
            if (isStartService) {
                Log.i("$TAG _MyService STOP and restart ", "isStartService: $isStartService")
                val myIntent = Intent(context, MyForegroundService::class.java)
                //if (!MyForegroundService.isMyForegroundServiceRunning) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startService(myIntent)
                    }
                    else {
                        context.startService(myIntent)
                    }
              //  }
            }
            else {
                Log.i("$TAG _MyService STOP ", "No Restart!!!!!")
                context.stopService(Intent(context, MyForegroundService::class.java))
            }
            Log.i("$TAG _MyService STOP ", " ELSE ")
        }
    }

    companion object {
        private val TAG = RestarterBroadcastReceiver::class.java.simpleName
    }
}