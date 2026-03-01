package io.shubham0204.smolchat.core

interface SmolLMClient {
    fun loadModel(modelId: String)
    fun getModelState(modelId: String): ModelStatus
}
