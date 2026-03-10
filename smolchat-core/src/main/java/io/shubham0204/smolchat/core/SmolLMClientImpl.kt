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

    @TestOnly
    internal suspend fun loadModelFromFd(fd: Int) {
        smolLM.loadFromFd(fd)
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

    override suspend fun loadModelFromBuffer(assetManager: android.content.res.AssetManager) {
        smolLM.loadFromBuffer(assetManager)
        state.value = ModelStatus.LOADED_IN_MEMORY("buffer_model")
    }

    override fun unloadModel() {
        smolLM.close()
    }
}
