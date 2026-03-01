package io.shubham0204.smollm.demo.di

import io.shubham0204.smolchat.core.FakeSmolLMClient
import io.shubham0204.smolchat.core.SmolLMClient
import io.shubham0204.smollm.demo.MainViewModel
import io.shubham0204.smollm.demo.ModelsRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SmolLMClient> { FakeSmolLMClient() }
    single { ModelsRepository(get()) }
    viewModel { MainViewModel(get()) }
}
