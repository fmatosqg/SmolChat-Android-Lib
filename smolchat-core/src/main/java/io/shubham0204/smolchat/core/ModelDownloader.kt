package io.shubham0204.smolchat.core

//import io.ktor.client.engine.cio.*

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.asSink
import java.io.File

class ModelDownloader(private val context: Context, private val httpClient: HttpClient) {


    suspend fun ensureModelDownloaded(
        url: String,
        modelName: String,
        onProgress: (progress: Float) -> Unit
    ): File {
        val modelDir = File(context.cacheDir, "model")
        if (!modelDir.exists()) modelDir.mkdirs()
        val outputFile = File(modelDir, modelName)

        if (outputFile.exists() && outputFile.length() > 0) {
            Log.i("ModelDownloader", "Model $modelName already exists at ${outputFile.absolutePath}. Skipping download.")
            onProgress(1f)
            return outputFile
        }

        Log.i("ModelDownloader", "Model $modelName not found or empty. Starting download from $url.")
        download(url, modelName, onProgress)
        return outputFile
    }

    suspend fun download(url: String, modelName: String, onProgress: (progress: Float) -> Unit) {

        val modelDir = File(context.cacheDir, "model")
        if (!modelDir.exists()) modelDir.mkdirs()

        val outputFile = File(modelDir, modelName)
        Log.i("ModelDownloader", "Downloading $url to ${outputFile.absolutePath}")


        val stream = outputFile.outputStream().asSink()
        val bufferSize: Long = 1024 * 1024

        httpClient.prepareGet(url).execute { httpResponse ->

            val channel: ByteReadChannel = httpResponse.body()
            var count = 0L
            var lastProgressPercent = 0
            stream.use {
                while (!channel.exhausted()) {
                    val chunk = channel.readRemaining(bufferSize)
                    count += chunk.remaining

                    chunk.transferTo(stream)

                    val progress = count.toFloat() / (httpResponse.contentLength()?.toFloat() ?: 0f)
                    val currentPercent = (progress * 100).toInt()

                    // Only log and update UI if the percentage has changed
                    if (currentPercent != lastProgressPercent) {
                        lastProgressPercent = currentPercent

                        onProgress(progress)
                    }
                }
            }
        }


    }
}