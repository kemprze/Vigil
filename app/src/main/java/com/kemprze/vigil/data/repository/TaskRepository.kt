package com.kemprze.vigil.data.repository

import com.kemprze.vigil.data.TaskDao
import com.kemprze.vigil.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
     fun getTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    fun getSubtasksForTask(parentId: String): Flow<List<Task>> {
        return taskDao.getSubtasksForTask(parentId)
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}