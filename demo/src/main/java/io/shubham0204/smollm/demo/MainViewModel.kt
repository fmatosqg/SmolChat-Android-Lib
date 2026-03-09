package io.shubham0204.smollm.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smolchat.core.ModelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.properties.ReadOnlyProperty

class MainViewModel(
    private val repository: ModelsRepository
) : ViewModel() {

    companion object {
        private val POC_MODEL_URL =
            "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf"
        private val POC_MODEL_NAME = "smollm2-360m-instruct-q8_0.gguf"

    }

    data class UiState(
        val statusText: String,
        val aiResponse: String,
        val buttonCopy: String,
        internal val modelStatus: ModelStatus
    )

    private val uiState = MutableStateFlow(
        UiState(
            statusText = "",
            aiResponse = "",
            buttonCopy = "",
            modelStatus = ModelStatus.UNAVAILABLE,
        )
    )

    init {
        observeStateFlow()
    }


    @Composable
    fun collectAsState() = uiState.collectAsState()

    private fun observeStateFlow() {
        viewModelScope.launch {
            repository.getModelStateFlow().collect { newStatus ->
                val statusLabel = when (newStatus) {
                    is ModelStatus.UNAVAILABLE -> "Unavailable"
                    is ModelStatus.DOWNLOADING -> "Downloading (${(newStatus.progress * 100).toInt()}%)"
                    is ModelStatus.ON_DISK -> "On Disk"
                    is ModelStatus.LOADED_IN_MEMORY -> "Loaded"
                }

                val buttonCopy = when (newStatus) {
                    is ModelStatus.UNAVAILABLE -> "Download model"
                    is ModelStatus.DOWNLOADING -> "..."
                    is ModelStatus.ON_DISK -> "Load model"
                    is ModelStatus.LOADED_IN_MEMORY -> "Generate Response"
                }

                uiState.update {
                    it.copy(
                        modelStatus = newStatus,
                        statusText = statusLabel,
                        buttonCopy = buttonCopy
                    )
                }
            }
        }
    }

    fun onButtonClicked() {
        uiState.update { it.copy(statusText = "clicked") }


        // side effects
        viewModelScope.launch {
            when (uiState.value.modelStatus) {
                is ModelStatus.DOWNLOADING -> Unit // do nothing
                ModelStatus.ON_DISK -> Unit // do nothing
                ModelStatus.UNAVAILABLE -> repository.loadModel(POC_MODEL_NAME, POC_MODEL_URL)
                is ModelStatus.LOADED_IN_MEMORY -> {
                    uiState.update { it.copy(statusText = "Thinking...") }
                    val response = repository.generateResponse("hello")
                    uiState.update { it.copy(aiResponse = response, statusText = "Done") }
                }
            }
        }
    }

}
