package io.shubham0204.smollm.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smolchat.core.ModelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val repository: ModelsRepository
) : ViewModel() {

    companion object {
        private val POC_MODEL_URL =
            "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf"
        private val POC_MODEL_NAME = "smollm2-360m-instruct-q8_0.gguf"

    }

    data class UiState(
        val statusText: String = "initial",
        val modelStatus: ModelStatus = ModelStatus.UNAVAILABLE,
        val aiResponse: String = ""
    )

    val uiState = MutableStateFlow(UiState())

    init {
        observeStateFlow()
    }

    private fun observeStateFlow() {
        viewModelScope.launch {
            repository.getModelStateFlow().collect { newStatus ->
                uiState.update { it.copy(modelStatus = newStatus) }
            }
        }
    }

    fun onButtonClicked() {
        uiState.update { it.copy(statusText = "clicked") }

        viewModelScope.launch {
            when (uiState.value.modelStatus) {
                is ModelStatus.DOWNLOADING -> Unit // do nothing
                ModelStatus.ON_DISK -> Unit // do nothing
                ModelStatus.UNAVAILABLE -> repository.loadModel(POC_MODEL_NAME, POC_MODEL_URL)
                is ModelStatus.LOADED_IN_MEMORY -> {
                    uiState.update { it.copy(statusText = "Thinking...") }
                    val response = repository.generateResponse("hello")
                    uiState.update { it.copy(aiResponse = response) }
                }
            }
        }
    }
}
