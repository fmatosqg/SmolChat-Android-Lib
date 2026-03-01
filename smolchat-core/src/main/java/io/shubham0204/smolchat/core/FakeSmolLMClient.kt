package io.shubham0204.smolchat.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FakeSmolLMClient : SmolLMClient {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val state = MutableStateFlow(ModelStatus.UNAVAILABLE)

    override fun loadModel(modelId: String) {
        if (state.value != ModelStatus.UNAVAILABLE) return
        
        scope.launch {
            state.value = ModelStatus.DOWNLOADING
            delay(3000)
            state.value = ModelStatus.ON_DISK
            delay(3000)
            state.value = ModelStatus.LOADED_IN_MEMORY
        }
    }

    override fun getModelState(modelId: String): ModelStatus {
        return state.value
    }
}
