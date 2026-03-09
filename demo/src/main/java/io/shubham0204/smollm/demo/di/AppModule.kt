package io.shubham0204.smollm.demo.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.shubham0204.smolchat.core.SmolLMClientImpl
import io.shubham0204.smolchat.core.ModelDownloader
import io.shubham0204.smolchat.core.SmolLMClient
import io.shubham0204.smollm.demo.MainViewModel
import io.shubham0204.smollm.demo.ModelsRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        HttpClient(CIO) {
            engine {
                requestTimeout = 0 // we'll download very large files, so timeout is disabled
            }
        }
    }
    factory { ModelDownloader(get(), get()) }
    factory<SmolLMClient> { SmolLMClientImpl(get()) }

    single { ModelsRepository(get()) }
    viewModel { MainViewModel(get()) }
}
