package com.example.taskapp.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromReminder(value: Reminder): String {
        val gson = Gson()
        val type = object : TypeToken<Reminder>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toReminder(value: String): Reminder {
        val gson = Gson()
        val type = object : TypeToken<Reminder>() {}.type
        return gson.fromJson(value, type)
    }

}