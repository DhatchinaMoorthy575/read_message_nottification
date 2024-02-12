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

                serviceScope.launch {
                   // val time = Calendar.getInstance().time
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val current =  formatter.format(dateInMillis)//formatter.format(time)

                    val messageEntity = MessageEntity(sender = sender, messageBody = messageBody, date = current)
                  //  messageRepository.insertMessage(messageEntity)

                    if (mListener != null) {
                        mListener!!.messageReceived(sender, messageBody,messageEntity)
                    }
                    else {

                    }
                }

            }
        }

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