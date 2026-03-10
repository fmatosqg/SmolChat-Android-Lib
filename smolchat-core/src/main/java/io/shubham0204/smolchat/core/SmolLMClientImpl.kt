package io.shubham0204.smolchat.core

import io.shubham0204.smollm.SmolLM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import java.io.File

class SmolLMClientImpl(
    private val modelDownloader: ModelDownloader
) : SmolLMClient {
    private val state = MutableStateFlow<ModelStatus>(ModelStatus.UNAVAILABLE)

    private val smolLM = SmolLM()


    override fun getModelStateFlow(): Flow<ModelStatus> = state

    override suspend fun loadModel(modelName: String, modelUrl: String) {
        if (state.value != ModelStatus.UNAVAILABLE) return

        withContext(Dispatchers.IO) {
            modelDownloader.ensureModelDownloaded(
                modelName = modelName,
                url = modelUrl,
                onProgress = { progress ->
                    state.value = ModelStatus.DOWNLOADING(progress)
                }
            )
                ?.let {
                    state.value = ModelStatus.ON_DISK
                    loadModelFromFile(it)
                    state.value = ModelStatus.LOADED_IN_MEMORY(modelName)
                }
            // TODO handle failure status
        }
    }

    @TestOnly
    internal suspend fun loadModelFromFile(file: File) {
        smolLM.load(file.absolutePath)
    }

    override suspend fun generateResponse(prompt: String): String {
        return withContext(Dispatchers.Default) {
            try {
                smolLM.getResponse(prompt)
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    override fun addSystemPrompt(systemPrompt: String) {
        smolLM.addSystemPrompt(systemPrompt)
    }

    override fun addUserPrompt(userMessage: String) {
        smolLM.addUserMessage(userMessage)
    }

    override fun addAssistantResponse(assistantMessage: String) {
        smolLM.addAssistantMessage(assistantMessage)
    }

    override fun unloadModel() {
        smolLM.close()
    }
}
