package io.shubham0204.smolchat.core

import io.shubham0204.smollm.SmolLM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

    private suspend fun loadModelFromFile(file: File) {
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

    override fun unloadModel() {
        smolLM.close()
    }
}
