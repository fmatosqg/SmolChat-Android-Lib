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

class FakeSmolLMClient(
    private val context: android.content.Context,
    private val modelDownloader: ModelDownloader
) : SmolLMClient {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val state = MutableStateFlow<ModelStatus>(ModelStatus.UNAVAILABLE)
    private val lock = ReentrantLock()
    private val smolLM = SmolLM()
    private var downloadedFile: File? = null

    private val POC_MODEL_URL =
        "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf"
    private val POC_MODEL_NAME = "smollm2-360m-instruct-q8_0.gguf"

    override fun getModelStateFlow(): Flow<ModelStatus> = state

    override fun loadModel(modelId: String) {
        if (state.value != ModelStatus.UNAVAILABLE) return

        lock.withLock {
            scope.launch {
                downloadedFile = modelDownloader.ensureModelDownloaded(
                    url = POC_MODEL_URL,
                    modelName = POC_MODEL_NAME,
                    onProgress = { progress ->
                        state.value = ModelStatus.DOWNLOADING(progress)
                    }
                )
                state.value = ModelStatus.ON_DISK

                loadModelFromFile()

            }
        }
    }

    override fun loadModelFromFile() {
        scope.launch {
            val modelDir = File(context.cacheDir, "model")
            val file = File(modelDir, POC_MODEL_NAME)

            try {
                smolLM.load(file.absolutePath)
                state.value = ModelStatus.LOADED_IN_MEMORY
            } catch (e: Exception) {
                state.value = ModelStatus.UNAVAILABLE // Or a specific ERROR state
            }
        }
    }

    override suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            smolLM.getResponse(prompt)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun close() {
        smolLM.close()
    }
}
