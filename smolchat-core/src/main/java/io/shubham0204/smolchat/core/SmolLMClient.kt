package io.shubham0204.smolchat.core

import kotlinx.coroutines.flow.Flow
import java.io.File

interface SmolLMClient {
    fun loadModel(modelId: String)
    fun loadModelFromFile()
    suspend fun generateResponse(prompt: String): String

    fun getModelStateFlow(): Flow<ModelStatus>

}
