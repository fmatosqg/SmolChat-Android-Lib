package io.shubham0204.smollm.demo

import io.shubham0204.smolchat.core.ModelStatus
import io.shubham0204.smolchat.core.SmolLMClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class ModelsRepository(private val client: SmolLMClient) {

    fun triggerLoad(modelId: String) {
        client.loadModel(modelId)
    }

    fun getModelStateFlow(modelId: String): Flow<ModelStatus> = flow {
        while (true) {
            emit(client.getModelState(modelId))
            delay(200)
        }
    }.distinctUntilChanged()
}
