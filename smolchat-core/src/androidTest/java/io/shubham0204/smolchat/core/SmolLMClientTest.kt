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
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SmolLMClientTest {

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

        fd.fileDescriptor
        fd.parcelFileDescriptor.fd


        assertTrue("File descriptor should be valid", fd.parcelFileDescriptor.fd > 0)

    }
    @Test
    fun loadModelFromAssetFd() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val client = SmolLMClientImpl(mockk())

        context.assets.openFd("test_model.gguf").use { afd ->
            Log.i("SmolLMClientTest", "Opened asset file descriptor: fd=${afd.parcelFileDescriptor.fd}, startOffset=${afd.startOffset}, length=${afd.length}")
            client.loadModelFromFd(afd.parcelFileDescriptor.fd)

//            client.generateResponse("Hello world")
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
//        val fd = context.resources.openRawResourceFd(R.raw.test_model)

//        client.loadModelFromFd(fd.parcelFileDescriptor.fd)

        // Wait for the model to load (you might want to use a more robust synchronization mechanism)

        val response = client.generateResponse("Hello, how are you?")
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
    }
}
