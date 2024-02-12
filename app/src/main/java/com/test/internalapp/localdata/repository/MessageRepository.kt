package com.test.internalapp.localdata.repository

import com.test.internalapp.localdata.dao.MessageDao
import com.test.internalapp.localdata.model.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy

class MessageRepository(private val messageDao: MessageDao) {

    val getAllMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

   // val getAllMessagesdistincts: Flow<List<MessageEntity>> = messageDao.getAllMessages().distinctUntilChangedBy { it.first() }

    suspend fun insertMessage(messageEntity: MessageEntity) {
        messageDao.insertMessage(messageEntity)
    }

    suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    suspend fun deleteMessages(list: List<MessageEntity>) {
        messageDao.deleteMessages(list)
    }

    val getAllMessagesdistincts: Flow<List<MessageEntity>> = messageDao.getAllMessages().distinctUntilChanged { oldList, newList ->
        if (oldList.isEmpty() || newList.isEmpty()) {
            oldList == newList
        } else {
            oldList.first() == newList.first()
        }
    }

}