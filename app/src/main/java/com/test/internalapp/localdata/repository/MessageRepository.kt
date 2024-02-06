package com.test.internalapp.localdata.repository

import com.test.internalapp.localdata.dao.MessageDao
import com.test.internalapp.localdata.model.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class MessageRepository(private val messageDao: MessageDao) {

    val getAllMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    val getAllMessagesdistinct: Flow<List<MessageEntity>> = messageDao.getAllMessages().distinctUntilChanged()

    suspend fun insertMessage(messageEntity: MessageEntity) {
        messageDao.insertMessage(messageEntity)
    }

    suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    suspend fun deleteMessages(list: List<MessageEntity>) {
        messageDao.deleteMessages(list)
    }
}