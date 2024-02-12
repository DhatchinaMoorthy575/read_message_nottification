package com.test.internalapp.localdata.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.test.internalapp.localdata.converters.Converters
import kotlinx.parcelize.Parcelize


@Entity(tableName = "message")
@TypeConverters(Converters::class)
@Parcelize
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sender: String,
    val messageBody: String,
    val date: String
) : Parcelable