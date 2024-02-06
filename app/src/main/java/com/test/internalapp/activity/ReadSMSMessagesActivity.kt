package com.test.internalapp.activity

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.test.internalapp.databinding.ActivityReadSmsMessagesBinding


class ReadSMSMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadSmsMessagesBinding

    private val smsList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadSmsMessagesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, smsList)
        binding.listView.setAdapter(adapter)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.READ_SMS), READ_SMS_PERMISSION_CODE)
        }
        else {
            readSms()
        }
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
                smsList.add("Sender: $address\nMessage: $body")
            } while (cursor.moveToNext())
        }
        cursor?.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_SMS_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readSms()
                val adapter = binding.listView.adapter as ArrayAdapter<String>
                adapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val READ_SMS_PERMISSION_CODE = 1
    }
}


interface MessageListenerInterface {
    // creating an interface method for messages received.
    fun messageReceived(sender: String, messageBody: String)
}