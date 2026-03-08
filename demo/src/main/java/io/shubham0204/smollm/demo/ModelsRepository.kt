package io.shubham0204.smollm.demo

import io.shubham0204.smolchat.core.ModelStatus
import io.shubham0204.smolchat.core.SmolLMClient
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ModelsRepository(private val client: SmolLMClient) {

    suspend fun triggerLoad(modelId: String) {
        client.loadModel(modelId)
    }

    suspend fun generateResponse(prompt: String): String {
        return client.generateResponse(prompt)
    }

    @OptIn(FlowPreview::class)
    fun getModelStateFlow(modelId: String): Flow<ModelStatus> = client
        .getModelStateFlow()
        .debounce(timeout = 200.milliseconds)
        .distinctUntilChanged()
}
