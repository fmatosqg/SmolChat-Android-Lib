package io.shubham0204.smolchat.core

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.ToNumberPolicy
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

data class TopicRating(val topic: String, val rating: Float)
data class ClassificationResponse(
    val retryCount: Int,
    val prompt: String,
    val ratings: List<TopicRating>
)

@RunWith(AndroidJUnit4::class)
class SmolLMClientTest {

    /**
     * Copies an asset file from the APK to the app's internal model cache directory.
     * This directory matches the one used by [ModelDownloader].
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

    private suspend fun generateAndParseResponse(client: SmolLMClient): ClassificationResponse? {

        val reasoning = client.generateResponse("Reasoning:")
        Log.d("SmolLMClientTest", "Reasoning: $reasoning")

        val topics = listOf("politics", "sports", "technology")
            .map {
                """
                    { "topic" : "$it", "rating": <number between 0 and 1> },
                """.trimIndent()
            }
            .joinToString(separator = "\n")

        client.addSystemPrompt(
            """
            Now let's format our reasoning into a structured JSON object that strictly adheres to the following format, with no additional text:
            
            Example output format:
            {
              "ratings": [
                $topics
              ]
            }
        """.trimIndent()
        )


        val response = client.generateResponse("Result:")
        Log.d("SmolLMClientTest", "Raw LLM Response: $response")

        // Robust Extraction: Find the first '{' and last '}' to isolate the JSON
        val startIndex = response.indexOf('{')
        val endIndex = response.lastIndexOf('}')

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            Log.e("SmolLMClientTest", "No JSON object found in response")
            return null
        }

        val jsonPart = response.substring(startIndex, endIndex + 1)
        Log.d("SmolLMClientTest", "Extracted JSON: $jsonPart")

        return try {
            GsonBuilder().setLenient()
                .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
                .create()
                .fromJson(jsonPart, ClassificationResponse::class.java)
        } catch (e: Exception) {
            // it may happen so often we want to do statistics instead of paying attention
            Log.d("SmolLMClientTest", "Failed to parse JSON: ${e.message}")
            null
        }
    }

    private suspend fun generateClassificationWithRetry(
        client: SmolLMClient,
        retryCount: Int = 3,
        prompt: String
    ): ClassificationResponse {

        var count = 0
        repeat(retryCount) { attempt ->
            count++
            val result = generateAndParseResponse(client)
            if (result != null) return result

            Log.w(
                "SmolLMClientTest",
                "Attempt ${attempt + 1} failed. Adding encouragement and retrying..."
            )
            // We know it's a SmolLMClientImpl in this test
            (client as SmolLMClientImpl).addUserPrompt("The previous response was not valid JSON. Please try again and return ONLY the JSON object, with no other text.")
        }
        return ClassificationResponse(
            retryCount = count,
            prompt = prompt,
            ratings = listOf(TopicRating("error", -1f))
        )
    }

    @Test
    fun testClassifier() = runTest {
        val modelFile = copyAssetToModelDir("test_model.gguf", "classifier_model.gguf")
        val smolLMClient = SmolLMClientImpl(
            ModelDownloader(
                InstrumentationRegistry.getInstrumentation().targetContext,
                HttpClient()
            )
        )
        smolLMClient.loadModelFromFile(modelFile)

        val input = "voting is important in democracy"
        val topics = listOf("politics", "sports", "technology")

        smolLMClient.addSystemPrompt(
            """
            You are a helpful assistant that analyzes the relevance of a given input across different topics.
            You will analyze the input and rate how much it matches from weak to strong across these topics: ${
                topics.joinToString(
                    ", "
                )
            }.
        """.trimIndent()
        )

        smolLMClient.addUserPrompt("Input: $input")

        val classification =
            generateClassificationWithRetry(smolLMClient, retryCount = 3, prompt = input)
        Log.i("SmolLMClientTest", "Final Classification Result: $classification")

        assertNotNull("Parsed classification should not be null", classification)

    }
}
