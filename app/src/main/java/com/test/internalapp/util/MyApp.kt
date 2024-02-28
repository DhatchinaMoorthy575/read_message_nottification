package com.test.internalapp.util

import android.app.Application
import kotlin.concurrent.Volatile

class MyApp : Application() {


    init {
        instance = this
    }

    companion object {
        public var instance: MyApp? = null

        fun applicationContext(): MyApp {
            return instance as MyApp
        }
    }

    override fun onCreate() {
        super.onCreate()

    }

}