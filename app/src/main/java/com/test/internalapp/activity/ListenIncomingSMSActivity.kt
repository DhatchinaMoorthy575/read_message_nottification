package com.test.internalapp.activity

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.test.internalapp.databinding.ActivityListenIncomingSmsBinding
import com.test.internalapp.service.MyForegroundService
import com.test.internalapp.service.NotificationService


class ListenIncomingSMSActivity : AppCompatActivity(), MessageListenerInterface {

    private lateinit var binding: ActivityListenIncomingSmsBinding
    var myServiceIntent: Intent? = null
    var myService: MyForegroundService? = null
    private val REQUEST_SMS_PERMISSION = 123 // You can use any integer value
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListenIncomingSmsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intentB = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intentB.setData(Uri.parse("package:$packageName"))
        startActivity(intentB)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS),
                REQUEST_SMS_PERMISSION
            )
        }
        // adding bind listener for message receiver on below line.

        //notification
        checkAndStartNotificationService()

        //Service
        startBackgroundService()

        binding.idTVHeading.setOnClickListener {
            Toast.makeText(this, "${isServiceRunning()}", Toast.LENGTH_SHORT).show()
        }
    }
    /*private fun initializePermissionObj() {
        easyPermissions = EasyPermissions.Builder(this@ListenIncomingSMSActivity)
            .setPermissionType(EasyPermissions.PermissionType.valueOf( android.Manifest.permission.RECEIVE_SMS))
            .setOnPermissionListener(object : OnPermissionsListener {
                override fun onGranted() {
                    toast(getString(R.string.permission_granted))
                }

                override fun onDeclined(shouldRequestAgain: Boolean) {
                    toast(getString(R.string.permission_declined))
                    if (shouldRequestAgain) {
                        // You can request again by calling "easyPermissions?.launch()" here.
                    } else {
                        //Never ask again selected, dismissed the dialog, or device policy prohibits the app from having that permission
                        // For example, Settings dialog opened here.
                        showSettingsDialog()
                    }
                }
            })
            .build()
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now listen for incoming SMS
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
    private fun isServiceRunning(): Boolean {
        val pm: PackageManager = packageManager
        val info: PackageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES)
        val services: Array<ServiceInfo> = info.services
        for (service in services) {
            if (MyForegroundService::class.java.getName() == service.name) {
                return true
            }
        }
        return false
    }
    //Service
    private fun startBackgroundService() {
        try {
            myService = MyForegroundService()
            myServiceIntent = Intent(this@ListenIncomingSMSActivity, myService!!::class.java)
            /* if (!isMyServiceRunning(myService!!::class.java)) {
                 startService(myServiceIntent)
             }*/
            startService(myServiceIntent)
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    fun stopBackgroundService() {
        stopService(myServiceIntent)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("TAG", "isMyServiceRunning?" + true + "")
                return true
            }
        }
        Log.i("TAG", "isMyServiceRunning?" + false + "")

        return false
    }

    //notification
    private fun checkAndStartNotificationService() {
        val componentName = ComponentName(packageName, NotificationService::class.java.getName())
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!notificationManager.isNotificationListenerAccessGranted(componentName)) {
                val notificationAccessSettings: Intent =
                    Intent(Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS))

                notificationAccessSettings.putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    componentName.flattenToString()
                )

                startActivity(notificationAccessSettings)
            }
            else {
                Log.d("LOGGER_TAG", "App has notification access")

            }
        }
        else {
            promptUserToGrantPermission()
        }

        val intent2 = Intent(this, NotificationService::class.java)
        startService(intent2)
    }

    private fun promptUserToGrantPermission() {
        // Provide instructions to the user on how to grant the necessary permission
        // You may want to open a specific settings screen or guide the user to the right place
        // For example:
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)

        val intent2 = Intent(this, NotificationService::class.java)
        startService(intent2)
    }

    private fun promptUserToEnableInSettings() {
        // Provide instructions to the user on how to enable your app in Notification Listener settings
        // You may want to open a specific settings screen or guide the user to the right place
        // For example:
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)

        val intent2 = Intent(this, NotificationService::class.java)
        startService(intent2)
    }



    override fun onDestroy() {
        super.onDestroy()
        stopService(myServiceIntent)
    }

    override fun messageReceived(sender: String, messageBody: String) {
        binding.idTVMessage.text = "$sender:$messageBody"
    }
}