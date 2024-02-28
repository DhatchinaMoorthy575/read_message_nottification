package com.test.internalapp.rest

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.test.internalapp.util.MyApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


object RestAdapter {
    private const val CONNECTION_TIMEOUT: Long = 100
    private const val API_BASE_URL: String = ApiUrls.BASE_URL
    private val gson = GsonBuilder().setLenient().create()

    val adapter: (Context,String) -> RestService?
        get() = { context,baseUrl ->
            try {
                val client = okHttpClient
                val finalBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                Retrofit.Builder()

                    .baseUrl(finalBaseUrl)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    //.addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(RestService::class.java)
            }
            catch (e:Exception){
                showToastMessage(e.toString())
                null
            }

        }

    private val okHttpClient: OkHttpClient
        get() {
            val okClientBuilder = OkHttpClient.Builder()
            //val httpLoggingInterceptor = HttpLoggingInterceptor(interceptor)

            val logging = HttpLoggingInterceptor()
            // set your desired log level
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            // httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okClientBuilder.addInterceptor(logging)
            // okClientBuilder.addNetworkInterceptor(new  StethoInterceptor());
            okClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            okClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            okClientBuilder.writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
           // okClientBuilder.retryOnConnectionFailure(true)
            return okClientBuilder.build()
        }
    fun showToastMessage(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(MyApp.applicationContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}