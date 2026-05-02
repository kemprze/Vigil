package com.kemprze.vigil.data

import androidx.room.*
import com.kemprze.vigil.data.model.simpleTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<simpleTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: simpleTask)

    @Update
    suspend fun updateTask(task: simpleTask)

    @Delete
    suspend fun deleteTask(task: simpleTask)
}