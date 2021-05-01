package com.example.taskapp

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.example.taskapp.database.Reminder
import com.example.taskapp.database.Task

internal class TaskBuilder(var context: Context) : ContextWrapper(context)
{
    var pendingIntent: PendingIntent? = null
    var intent: Intent? = null
    var task: Task? = null

    fun createTaskWithReminder(text: String?, reminder: Reminder?, isImportant: Boolean, requestCode: Int) : Task
    {
        intent = Intent(context, Receiver::class.java)
        intent!!.putExtra("message", text)
        pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0)
        return Task(text!!, reminder!!, isImportant, pendingIntentIndex = requestCode)
    }
    fun createTaskWithoutReminder(text: String?, isImportant: Boolean) : Task
    {
        return Task(text!!, Reminder(false, 0, 0, 0, 0), isImportant)
    }
}