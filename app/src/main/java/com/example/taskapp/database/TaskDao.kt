package com.example.taskapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean, showCompleted: Boolean) : Flow<List<Task>>
    {
        if(showCompleted) {
            return getCompletedTasks()
        }
        return when(sortOrder) {
            SortOrder.SORT_BY_NAME -> getTasksSortByName(query, hideCompleted)
            SortOrder.SORT_BY_DATE -> getTasksSortByDate(query, hideCompleted)
        }
    }

    @Query("SELECT * FROM task_table WHERE completed =  1")
    fun getCompletedTasks() : Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name")
    fun getTasksSortByName(searchQuery: String, hideCompleted: Boolean) : Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortByDate(searchQuery: String, hideCompleted: Boolean) : Flow<List<Task>>

    @Query("select * from task_table")
    suspend fun allTasks() : List<Task>

    @Query("delete from task_table")
    suspend fun deleteAllTasks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task?)


}