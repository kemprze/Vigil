package com.kemprze.vigil.data.model

data class SimpleTaskList(
    val taskList: MutableList<SimpleTask> = mutableListOf(),
    val finishedTaskList: MutableList<SimpleTask> = mutableListOf()
)
