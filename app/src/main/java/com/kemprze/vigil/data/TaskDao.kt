package com.kemprze.vigil.data

import androidx.room.*
import com.kemprze.vigil.data.model.SimpleTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<SimpleTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: SimpleTask)

    @Update
    suspend fun updateTask(task: SimpleTask)

    @Delete
    suspend fun deleteTask(task: SimpleTask)
}