package com.test.internalapp.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
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
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.permissionx.guolindev.PermissionX
import com.test.internalapp.databinding.ActivityListenIncomingSmsBinding
import com.test.internalapp.service.MyForegroundService
import com.test.internalapp.service.NotificationService


class ListenIncomingSMSActivity : AppCompatActivity(), MessageListenerInterface {

    private lateinit var binding: ActivityListenIncomingSmsBinding
    var myServiceIntent: Intent? = null
    var myService: MyForegroundService? = null
    private val REQUEST_SMS_PERMISSION = 123
    var finishSplashScreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityListenIncomingSmsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupPreDrawListener()
        /* val intentB = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
         intentB.setData(Uri.parse("package:$packageName"))
         startActivity(intentB)*/

        //requestIgnoreBatteryOptimizations()
        requestMYPermissions()

        //notification
        checkAndStartNotificationService()

        //Service
        startBackgroundService()

        binding.idTVHeading.setOnClickListener {
            Toast.makeText(this, "${isServiceRunning()}", Toast.LENGTH_SHORT).show()
        }
    /*    val notificationIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val expandedNotificationText = String.format(
            "Background activity is restricted on this device.\nPlease allow it so we can post an active notification during work sessions.\n\nTo do so, click on the notification to go to\nApp management -> search for %s -> Battery Usage -> enable 'Allow background activity')",
            getString(R.string.autofill)
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "CHANNEL_ID144ww")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentText("Service is running in the background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedNotificationText))

        val channel = NotificationChannel("CHANNEL_ID144ww", "MyForegroundService", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = builder.build()

        notificationManager.notify(764, notification)
        //requestIgnoreBatteryOptimizations()*/
    }
    private fun setupPreDrawListener() {
        // Set up an OnPreDrawListener to the root view.
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                  /*  // Check if the initial data is ready.
                    val isReady = true
                    return if (isReady) {
                        // The content is ready; start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready; suspend.
                        false
                    }
*/

                    // Check if the initial data is ready.
                    return if (finishSplashScreen) {
                        // The content is ready; start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)

                        true
                    }
                    else {
                        // The content is not ready; suspend.
                        false
                    }
                }
            }
        )
        // 5 seconds timeout to hide splash screen
        Handler(Looper.getMainLooper()).postDelayed({ finishSplashScreen = true }, 5000)
    }
    private fun exitWithSlideUp(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                // Create your custom animation.
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 200L

                // Call SplashScreenView.remove at the end of your custom animation.
                slideUp.doOnEnd { splashScreenView.remove() }

                // Run your animation.
                slideUp.start()
            }
        }
    }
    fun requestIgnoreBatteryOptimizations() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(packageName)
        if (!isIgnoringBatteryOptimizations) {
            val intent = Intent()
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:$packageName"))
            //startActivityForResult(intent, MY_IGNORE_OPTIMIZATION_REQUEST)
            resultLauncher.launch(intent)


           /* val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            }*/
        }


    }

    private fun requestMYPermissions() {
        val PERMISSIONS_LIST = ArrayList<String>()
        PERMISSIONS_LIST.addAll(
            listOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PERMISSIONS_LIST.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PERMISSIONS_LIST.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PERMISSIONS_LIST.add(Manifest.permission.START_FOREGROUND_SERVICES_FROM_BACKGROUND)
        }

        PERMISSIONS_LIST.add(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND)
        PERMISSIONS_LIST.add(Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS_LIST.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        PermissionX.init(this)
            .permissions(PERMISSIONS_LIST)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    //Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                    Log.d("LOGGER_TAG", "All permissions are granted")
                } else {
                    Log.e("LOGGER_TAG", "These permissions are denied: $deniedList")
                    //Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                }

                requestIgnoreBatteryOptimizations()
            }

    }

    override fun onResume() {
        super.onResume()

    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            //doSomeOperations()
        }
        else if (result.resultCode == Activity.RESULT_CANCELED) {
           // requestIgnoreBatteryOptimizations()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now listen for incoming SMS
            }
            else {
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
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)

        val intent2 = Intent(this, NotificationService::class.java)
        startService(intent2)
    }

    private fun promptUserToEnableInSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)

        val intent2 = Intent(this, NotificationService::class.java)
        startService(intent2)
    }


    override fun onDestroy() {
        super.onDestroy()
        //stopService(myServiceIntent)
    }

    override fun messageReceived(sender: String, messageBody: String) {
        binding.idTVMessage.text = "$sender:$messageBody"
    }
}