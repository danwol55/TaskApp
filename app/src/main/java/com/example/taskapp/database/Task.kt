package com.example.taskapp.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Parcelize
@Entity(tableName = "task_table")
data class Task(
    val name: String,
    @TypeConverters(Converters::class)
    val reminder: Reminder,
    val important: Boolean = false,
    val completed: Boolean = false,
    val pendingIntentIndex: Int = -1,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    val dateFormatCreated: String get() = DateFormat.getDateTimeInstance().format(created)
}
