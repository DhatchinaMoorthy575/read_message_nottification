package com.test.internalapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.test.internalapp.activity.MessageListenerInterface
import com.test.internalapp.localdata.AppDatabase
import com.test.internalapp.localdata.repository.MessageRepository
import com.test.internalapp.localdata.model.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MessageBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val database by lazy { AppDatabase.getDatabase(context) }
        val messageRepository by lazy { MessageRepository(database.MessageDao()) }

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                val sender = smsMessage.displayOriginatingAddress.toString()
                val messageBody = smsMessage.messageBody.toString()
                val dateInMillis= smsMessage.timestampMillis

                val message = "Telephony.Sms Sender : " + smsMessage.displayOriginatingAddress + "Message: " + smsMessage.messageBody
                /*if (mListener != null) {
                    mListener!!.messageReceived(sender, messageBody)
                }
                else {

                }*/
                serviceScope.launch {
                    val time = Calendar.getInstance().time
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val current =  formatter.format(dateInMillis)//formatter.format(time)

                    val messageEntity = MessageEntity(sender = sender, messageBody = messageBody, date = current)
                    messageRepository.insertMessage(messageEntity)
                }
            }
        }
/*
                // getting bundle data on below line from intent.
                val data = intent.extras
                // creating an object on below line.
               // val pdus = data!!["pdus"] as Array<Any>?
                val pdus = data?.get("pdus") as? Array<*>
                // running for loop to read the sms on below line.
                for (i in pdus!!.indices) {
                    // getting sms message on below line.
                    val smsMessage: SmsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray, String())
                    // extracting the sms from sms message and setting it to string on below line.
                    val message = "Sender : " + smsMessage.displayOriginatingAddress + "Message: " + smsMessage.messageBody
                    // adding the message to listener on below line.
                    if (mListener!=null) {
                        mListener!!.messageReceived(smsMessage.displayOriginatingAddress ,smsMessage.messageBody)
                    }
                }*/
    }

    companion object {
        // creating a variable for a message listener interface on below line.
        private var mListener: MessageListenerInterface? = null

        // on below line we are binding the listener.
        fun bindListener(listener: MessageListenerInterface?) {
            mListener = listener
        }

        private val serviceJob = Job()
        private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    }
}