package com.kemprze.vigil.data.repository

import com.kemprze.vigil.data.TaskDao
import com.kemprze.vigil.data.model.SimpleTask
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
     fun getTasks(): Flow<List<SimpleTask>> {
        return taskDao.getAllTasks()
    }

    suspend fun insertTask(task: SimpleTask) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: SimpleTask) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: SimpleTask) {
        taskDao.deleteTask(task)
    }
}