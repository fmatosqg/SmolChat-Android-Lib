package io.shubham0204.smollm.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smolchat.core.ModelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ModelsRepository
) : ViewModel() {

    data class UiState(
        val statusText: String = "initial",
        val modelStatus: ModelStatus = ModelStatus.UNAVAILABLE
    )

    private val uiMutableState = MutableStateFlow(UiState())
    val uiState = uiMutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getModelStateFlow("some-model-id").collect { newStatus ->
                uiMutableState.update { it.copy(modelStatus = newStatus) }
            }
        }
    }

    fun onButtonClicked() {
        repository.triggerLoad("some-model-id")
        uiMutableState.update { it.copy(statusText = "clicked") }
    }
}
