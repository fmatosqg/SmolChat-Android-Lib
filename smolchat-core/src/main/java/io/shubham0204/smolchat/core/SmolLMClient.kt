package io.shubham0204.smolchat.core

import kotlinx.coroutines.flow.Flow
import java.io.File

interface SmolLMClient {
    suspend fun loadModel(modelId: String)

    suspend fun generateResponse(prompt: String): String

    fun getModelStateFlow(): Flow<ModelStatus>

}
