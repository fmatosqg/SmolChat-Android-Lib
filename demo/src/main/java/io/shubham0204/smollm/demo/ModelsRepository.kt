package io.shubham0204.smollm.demo

import io.shubham0204.smolchat.core.ModelStatus
import io.shubham0204.smolchat.core.SmolLMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ModelsRepository(private val client: SmolLMClient) {

    suspend fun loadModel(modelName: String, modelUrl: String) {
        client.loadModel(modelName = modelName, modelUrl = modelUrl)
    }

    suspend fun generateResponse(prompt: String): String {
        return client.generateResponse(prompt)
    }

    @OptIn(FlowPreview::class)
    fun getModelStateFlow(): Flow<ModelStatus> = client
        .getModelStateFlow()
        .sample(200.milliseconds)
        .distinctUntilChanged()
}
