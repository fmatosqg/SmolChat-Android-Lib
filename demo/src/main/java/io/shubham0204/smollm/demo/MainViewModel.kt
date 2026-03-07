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

    data class UiState(
        val statusText: String = "initial",
        val modelStatus: ModelStatus = ModelStatus.UNAVAILABLE,
        val aiResponse: String = ""
    )

    val uiState = MutableStateFlow(UiState())

    init {
        viewModelScope.launch {
            repository.getModelStateFlow("some-model-id").collect { newStatus ->
                uiState.update { it.copy(modelStatus = newStatus) }
                
                // Automatically transition from ON_DISK to loading in memory
                if (newStatus is ModelStatus.ON_DISK) {
                    // In a real app, we'd get the actual file path
                    repository.loadFromFile(File("dummy-path"))
                }
                
                // If loaded, trigger the prompt "hello"
                if (newStatus is ModelStatus.LOADED_IN_MEMORY && uiState.value.aiResponse.isEmpty()) {
                    val response = repository.generateResponse("hello")
                    uiState.update { it.copy(aiResponse = response) }
                }
            }
        }
    }

    fun onButtonClicked() {
        repository.triggerLoad("some-model-id")
        uiState.update { it.copy(statusText = "clicked") }
    }
}
