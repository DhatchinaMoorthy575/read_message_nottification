package com.test.internalapp.localdata.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.test.internalapp.localdata.model.StatusBarNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusBarNotificationDao {

    @Query("SELECT * FROM status_bar_notifications")
    fun getAllNotifications(): Flow<List<StatusBarNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: StatusBarNotificationEntity)

    @Query("DELETE FROM status_bar_notifications")
    suspend fun deleteAllNotifications()


    @Delete
    suspend fun deleteNotifications(list: List<StatusBarNotificationEntity>)
}