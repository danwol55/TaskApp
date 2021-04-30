package com.example.taskapp.dependencyInjection

import android.app.Application
import androidx.room.Room
import com.example.taskapp.database.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_table")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()


    @Provides
    fun provideDao(db: TaskDatabase) = db.taskDao()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideScope() = CoroutineScope(SupervisorJob())
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope