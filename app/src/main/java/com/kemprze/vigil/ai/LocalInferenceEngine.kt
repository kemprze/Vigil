package com.kemprze.vigil.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.withContext


class LocalInferenceEngine(private val context: Context) {
    private var engine: Engine? = null
    fun isReady(): Boolean = engine != null

    suspend fun initialize(modelPath: String) {
        android.util.Log.d("InferenceEngine", "Initializing model from $modelPath")
        withContext(Dispatchers.IO) {
            val engineConfig = EngineConfig(
                modelPath = modelPath,
                backend = Backend.CPU(),
                cacheDir = context.cacheDir.path
            )

            engine = Engine(engineConfig)
            engine?.initialize()
        }
        android.util.Log.d("InferenceEngine", "Model ready!")
    }

    suspend fun suggestSubtasks(
        taskName: String,
        taskDescription: String
    ): List<String> {
        return withContext(Dispatchers.IO) {
            val e = engine ?: return@withContext emptyList<String>()

            val conversationConfig = ConversationConfig(
                systemInstruction = Contents.of(
                    "You are a task planning assistant. " +
                            "When given a task, you break it down into concrete, actionable subtasks. " +
                            "Reply with a numbered list only. " +
                            "No explanation, no preamble."
                ),
                samplerConfig = SamplerConfig(topK = 40, topP = 0.95, temperature = 0.7)
            )

            var subtasks = emptyList<String>()
            e.createConversation(conversationConfig).use {
                conversation ->
                val prompt = "Break down this task into 3-5 subtasks.\nTask: $taskName\nDescription: ${taskDescription.ifEmpty {"No description"}}"
                val message = conversation.sendMessage(prompt)
                val result = message.contents.toString() ?: ""
                subtasks = parseSubtasks(result)
            }
            subtasks
        }
    }



    private fun parseSubtasks(response: String): List<String> {
        return response.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map {
                line -> line.replace(Regex("^\\d+[.)\\s]+"), "")
                .replace(Regex("^[-]\\s*"), "")
                .trim()
            }
            .filter { it.isNotBlank() }
    }

    fun close() {
        engine?.close()
        engine = null
    }
}