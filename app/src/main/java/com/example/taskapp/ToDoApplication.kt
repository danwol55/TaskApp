package com.example.taskapp

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ToDoApplication : Application()
{

    override fun onCreate()
    {
        super.onCreate()
        appContext = applicationContext
    }

    companion object
    {

        lateinit var appContext: Context

    }
}
