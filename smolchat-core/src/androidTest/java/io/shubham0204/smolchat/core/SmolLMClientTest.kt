package io.shubham0204.smolchat.core

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.shubham0204.smolchat.core.test.R
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

import io.shubham0204.smollm.GGUFReader
import java.nio.channels.FileChannel
import java.io.FileInputStream

@RunWith(AndroidJUnit4::class)
class SmolLMClientTest {

    @Test
    fun loadModelFromBuffer() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        
        // POC: Passing AssetManager instead of ByteBuffer
        val client = SmolLMClientImpl(mockk())
        client.loadModelFromBuffer(context.assets)
        
        val response = client.generateResponse("Hello")
        Log.i("SmolLMClientTest", "AI Response from AssetManager: $response")
        assertNotNull("Response should not be null", response)
        assertTrue("Response should not be empty", response.isNotEmpty())
        assertFalse("Should not be error: $response", response.startsWith("Error:"))
    }

    @Test
    fun testAssetExists() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val assetManager = context.assets
        val inputStream = assetManager.open("test_model.gguf")
        assertNotNull(inputStream)
        inputStream.close()
    }

    @Test
    fun another() {
        val context = InstrumentationRegistry.getInstrumentation().context

        val afd = context.assets.openFd("test_model.gguf")
        val fd = afd.parcelFileDescriptor.fd // This is the integer you pass to JNI

        assertTrue("File descriptor should be valid", fd > 0)
    }

    @Test
    fun yetanother() {
        val context = InstrumentationRegistry.getInstrumentation().context

        val fd = context.resources.openRawResourceFd(R.raw.test_model)

        assertTrue("File descriptor should be valid", fd.parcelFileDescriptor.fd > 0)
    }

    @Test
    fun loadModelFromAssetFd() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val client = SmolLMClientImpl(mockk())

        context.assets.openFd("test_model.gguf").use { afd ->
            Log.i("SmolLMClientTest", "Opened asset file descriptor: fd=${afd.parcelFileDescriptor.fd}, startOffset=${afd.startOffset}, length=${afd.length}")
            client.loadModelFromFd(afd.parcelFileDescriptor.fd)
        }
    }

    @Test
    fun loadModelFromResFd() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val client = SmolLMClientImpl(mockk())

        context.resources.openRawResourceFd(R.raw.test_model).use { afd ->
            Log.i("SmolLMClientTest", "Opened asset file descriptor: fd=${afd.parcelFileDescriptor.fd}, startOffset=${afd.startOffset}, length=${afd.length}")
            client.loadModelFromFd(afd.parcelFileDescriptor.fd)
        }

        val response = client.generateResponse("Hello, how are you?")
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
    }
}
