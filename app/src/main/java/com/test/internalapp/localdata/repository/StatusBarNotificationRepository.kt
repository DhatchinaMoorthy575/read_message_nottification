package com.test.internalapp.localdata.repository

import androidx.room.Transaction
import com.test.internalapp.localdata.dao.StatusBarNotificationDao
import com.test.internalapp.localdata.model.MessageEntity
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class StatusBarNotificationRepository(private val statusBarNotificationDao: StatusBarNotificationDao) {

    val allNotificationsdistinct: Flow<List<StatusBarNotificationEntity>> = statusBarNotificationDao.getAllNotifications().distinctUntilChanged()


    val allNotifications: Flow<List<StatusBarNotificationEntity>> = statusBarNotificationDao.getAllNotifications()

    @Transaction
    suspend fun insert(notification: StatusBarNotificationEntity) {
        statusBarNotificationDao.insertNotification(notification)
    }

    suspend fun deleteAll() {
        statusBarNotificationDao.deleteAllNotifications()
    }

    suspend fun deleteNotifications(list: List<StatusBarNotificationEntity>) {
        statusBarNotificationDao.deleteNotifications(list)
    }

    val allNotificationsdistincts: Flow<List<StatusBarNotificationEntity>> = statusBarNotificationDao.getAllNotifications().distinctUntilChanged { oldList, newList ->
        if (oldList.isEmpty() || newList.isEmpty()) {
            oldList == newList
        } else {
            oldList.first() == newList.first()
        }
    }
}