package io.shubham0204.smolchat.core

sealed class ModelStatus {
    object UNAVAILABLE : ModelStatus()
    data class DOWNLOADING(val progress: Float) : ModelStatus()
    object ON_DISK : ModelStatus()
    object LOADED_IN_MEMORY : ModelStatus()
}
