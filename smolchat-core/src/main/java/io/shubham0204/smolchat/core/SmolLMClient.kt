package io.shubham0204.smolchat.core

import kotlinx.coroutines.flow.Flow
import java.io.File

interface SmolLMClient {

    suspend fun loadModel(modelName: String, modelUrl: String)

    fun unloadModel()

    fun getModelStateFlow(): Flow<ModelStatus>

    suspend fun generateResponse(prompt: String): String
}
