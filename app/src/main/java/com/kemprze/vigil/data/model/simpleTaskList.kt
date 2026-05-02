package com.kemprze.vigil.data.model

data class simpleTaskList(
    val taskList: MutableList<simpleTask> = mutableListOf(),
    val finishedTaskList: MutableList<simpleTask> = mutableListOf()
)
