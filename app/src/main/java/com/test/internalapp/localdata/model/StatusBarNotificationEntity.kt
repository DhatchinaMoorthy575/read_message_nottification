package com.test.internalapp.localdata.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import com.test.internalapp.localdata.converters.Converters
import kotlinx.parcelize.Parcelize


@Entity(tableName = "status_bar_notifications",
    primaryKeys = [
        "packageName",
        "ticker",
        "title",
        "text",
        "subtext",
   ])
@TypeConverters(Converters::class)
@Parcelize
data class StatusBarNotificationEntity(
   /* @PrimaryKey(autoGenerate = true)*/ val id: Int = 0,
    @ColumnInfo("packageName")
    val packageName: String,
    @ColumnInfo("ticker")
    val ticker: String,
    @ColumnInfo("title")
    val title: String,
    @ColumnInfo("text")
    val text: String,
    @ColumnInfo("subtext")
    val subtext: String,
    val smallIconId: Bitmap?,
    val largeIcon: Bitmap?,
   val datetime: String,
    //val extras: String,
    //val sbn: StatusBarNotification

) : Parcelable {

}