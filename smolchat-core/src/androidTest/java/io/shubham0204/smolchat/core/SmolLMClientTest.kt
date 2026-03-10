package io.shubham0204.smolchat.core

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class SmolLMClientTest {

    /**
     * Copies an asset file from the APK to the app's internal model cache directory.
     * This directory matches the one used by [ModelDownloader].
     *
     * @param sourceName The relative path to the asset within the 'assets' directory.
     * @param destinationName The filename to use in the internal storage.
     * @return The [File] object pointing to the copied model.
     */
    private suspend fun copyAssetToModelDir(sourceName: String, destinationName: String): File {
        return withContext(Dispatchers.IO) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val modelDir = File(context.cacheDir, "model")
            if (!modelDir.exists()) modelDir.mkdirs()

            val outputFile = File(modelDir, destinationName)
            val assetContext = InstrumentationRegistry.getInstrumentation().context

            assetContext.assets.open(sourceName).use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        }
    }

    @Test
    fun testAssetExists() {
        Log.i("SmolLMClientTest", "Checking if test model asset exists...")
        val context = InstrumentationRegistry.getInstrumentation().context
        val assetManager = context.assets
        val inputStream = assetManager.open("test_model.gguf")
        assertNotNull(inputStream)
        inputStream.close()
    }

    @Test
    fun testLoadModelFromAsset() = runTest {
        val modelFile = copyAssetToModelDir("test_model.gguf", "test_model_copied.gguf")
        assertTrue("Model file was not copied successfully",modelFile.exists())
        Log.i("SmolLMClientTest", "Model file copied to: ${modelFile.absolutePath} - ${modelFile.length() / 1024 / 1024} MB")

        val smolLMClient = SmolLMClientImpl(
            ModelDownloader(
                InstrumentationRegistry.getInstrumentation().targetContext,
                HttpClient()
            )
        )
        smolLMClient.loadModelFromFile(modelFile)

        val response = smolLMClient.generateResponse("Hello")

        Log.i("SmolLMClientTest", "Generated response: $response")
        assertTrue("Response should not be empty", response.isNotEmpty())
    }
}
