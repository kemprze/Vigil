package com.kemprze.vigil.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext


class LocalInferenceEngine(private val context: Context) {
    private var engine: Engine? = null
    private val engineMutex = Mutex()
    fun isReady(): Boolean = engine != null

    suspend fun initialize(modelPath: String) {

        engineMutex.withLock {
            if (isReady()) {
                return@withLock
            } else {
                android.util.Log.d("InferenceEngine", "Initializing model from $modelPath")
                withContext(Dispatchers.IO) {
                    val gpuEngine = try {
                        Engine(
                            EngineConfig(
                                modelPath = modelPath,
                                backend = Backend.GPU(),
                                cacheDir = context.cacheDir.path
                            )
                        ).also { it.initialize() }
                    } catch (e: Exception) {
                        null
                    }

                    engine = gpuEngine ?: Engine(
                        EngineConfig(
                            modelPath = modelPath,
                            backend = Backend.CPU(),
                            cacheDir = context.cacheDir.path
                        )
                    ).also { it.initialize() }
                }
                android.util.Log.d("InferenceEngine", "Model ready!")
            }
        }
    }

    suspend fun suggestSubtasks(
        taskName: String,
        taskDescription: String,
        feedbackStyle: String
    ): List<String> {
        engineMutex.withLock {
            return withContext(Dispatchers.IO) {
                val e = engine ?: return@withContext emptyList<String>()

                val conversationConfig = ConversationConfig(
                    systemInstruction = Contents.of(
                        "You are a task planning assistant who provides feedback in $feedbackStyle. " +
                                "When given a task, you break it down into short, concrete, actionable subtasks. " +
                                "Reply with a numbered list only. " +
                                "No explanation, no preamble."
                    ),
                    samplerConfig = SamplerConfig(topK = 40, topP = 0.95, temperature = 0.7)
                )

                var subtasks = emptyList<String>()
                e.createConversation(conversationConfig).use { conversation ->
                    val prompt =
                        "Break down this task into 3-5 subtasks.\nTask: $taskName\nDescription: ${taskDescription.ifEmpty { "No description" }}"
                    val message = conversation.sendMessage(prompt)
                    val result = message.contents.toString() ?: ""
                    subtasks = parseSubtasks(result)
                }
                subtasks
            }
        }
    }

    suspend fun suggestCategory(taskName: String): String {
        engineMutex.withLock {
            return withContext(Dispatchers.IO) {
                val e = engine ?: return@withContext "NONE"
                val conversationConfig = ConversationConfig(
                    systemInstruction = Contents.of(
                        "You are a task classifier. " +
                                "Reply with exactly one word."
                    ),
                    samplerConfig = SamplerConfig(topK = 1, topP = 1.0, temperature = 0.1)
                )
                var result = "NONE"
                e.createConversation(conversationConfig).use { conversation ->
                    val prompt = "Classify this task into exactly one of these categories: " +
                            "WORK, PERSONAL, SHOPPING, HEALTH, HOME, EDUCATION, FINANCE, OTHER. " +
                            "Reply with just the category case in uppercase, nothing else. Task: $taskName"
                    val message = conversation.sendMessage(prompt)
                    result = message.contents.toString().trim().uppercase()
                }

                result
            }
        }
    }


    private fun parseSubtasks(response: String): List<String> {
        return response.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                line.replace(Regex("^\\d+[.)\\s]+"), "")
                    .replace(Regex("^[-]\\s*"), "")
                    .trim()
            }
            .filter { it.isNotBlank() }
    }

    fun close() {
        engine?.close()
        engine = null
    }

    suspend fun generateInsight(
        total: Int,
        completed: Int,
        pending: Int,
        topCategory: String,
        feedbackStyle: String
    ): String {
        engineMutex.withLock {
            return withContext(Dispatchers.IO) {
                val e = engine ?: return@withContext "No insights so far."

                val conversationConfig = ConversationConfig(
                    systemInstruction = Contents.of(
                        "You are a productivity assistant. " +
                                "The user has $total tasks: $completed completed and $pending pending. " +
                                "Their most active category is $topCategory. Give a short " +
                                "insight in 2-3 sentences, no more. " +
                                "If there are no tasks, give a $feedbackStyle message to get started."
                    ),
                    samplerConfig = SamplerConfig(topK = 40, topP = 0.95, temperature = 0.7)
                )
                var result = "NONE"
                e.createConversation(conversationConfig).use { conversation ->
                    val prompt = "Based on the provided data, give me my productivity insights. " +
                            "Focus on providing objective information, but remember about being positive. " +
                            "Make the tone feel non-judgemental and $feedbackStyle."
                    val message = conversation.sendMessage(prompt)
                    result = message.contents.toString().trim()
                }

                result
            }
        }
    }
}