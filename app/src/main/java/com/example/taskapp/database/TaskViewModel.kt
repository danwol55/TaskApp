package com.example.taskapp.database

import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskapp.ToDoApplication.Companion.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val searchTask = MutableStateFlow("")
    val preferencesFlow = preferenceManager.preferencesFlow

    private val taskFlow = combine(
        searchTask,
        preferencesFlow
    ) {searchTask, preferences ->
        Pair(searchTask, preferences)}.flatMapLatest {
        (searchTask, preferences) -> taskDao.getTasks(searchTask, preferences.sortOrder, preferences.hideCompleted, preferences.showCompleted)
    }

    val tasks = taskFlow.asLiveData()

    fun addTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }
    fun deleteTask(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
    }
    fun onSortOrder(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }
    fun onHideCompleted(hideCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted)
    }
    fun onShowCompleted(showCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateShowCompleted(showCompleted)
    }
    fun onTaskCheck(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }
    fun deleteAllTasks() = viewModelScope.launch {
        taskDao.deleteAllTasks()
    }
    fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
    }
    fun disableReminderForTask(index: Int) = viewModelScope.launch {
        val task: Task?
        val tasks1 = tasks.value

        if (tasks1 != null)
        {
            for(i in tasks1)
            {
                if(i.pendingIntentIndex == index)
                {
                    task = i
                    val taskToUpdate = task.copy(reminder = Reminder(false, 0, 0, 0, 0), pendingIntentIndex = -1)
                    updateTask(taskToUpdate)

                    Toast.makeText(appContext, "task reminder updated", Toast.LENGTH_LONG).show()
                    break
                }
            }
        }
    }
}